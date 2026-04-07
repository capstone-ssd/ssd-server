#!/usr/bin/env python3
import json
import os
import sys
import textwrap
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

# PR 코멘트를 재사용하기 위한 식별자와 리뷰 입력 크기 제한값들이다.
MARKER = "<!-- codex-pr-review -->"
MAX_FILES = 40
MAX_TOTAL_PATCH_CHARS = 80000
MAX_PATCH_CHARS_PER_FILE = 6000
MAX_AGENTS_CHARS = 24000
COMMENT_HEADER = "## Codex PR Review"

# 바이너리/생성물/리뷰 가치가 낮은 파일은 비용 절감을 위해 제외한다.
SKIP_SUFFIXES = {
    ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".ico", ".pdf", ".jar", ".class",
    ".lock", ".iml", ".mp4", ".mov", ".zip", ".gz", ".tgz", ".woff", ".woff2",
}
SKIP_FILENAMES = {
    "gradlew",
}
SKIP_PATH_PARTS = {
    "build",
    ".gradle",
    "node_modules",
}
SEVERITY_ORDER = {"high": 0, "medium": 1, "low": 2}


def fail(message: str) -> None:
    # GitHub Actions 로그에 즉시 실패 원인을 남기고 종료한다.
    print(message, file=sys.stderr)
    sys.exit(1)


def read_json(path: str) -> dict[str, Any]:
    # GitHub event payload 같은 로컬 JSON 파일을 읽는다.
    with open(path, "r", encoding="utf-8") as handle:
        return json.load(handle)


class GitHubClient:
    # PR 파일 목록 조회, 기존 코멘트 조회/갱신에 필요한 최소 GitHub API 래퍼다.
    def __init__(self, repo_full_name: str, token: str) -> None:
        self.repo_full_name = repo_full_name
        self.token = token
        self.base_url = "https://api.github.com"

    def request(self, method: str, path: str, payload: dict[str, Any] | None = None) -> Any:
        # 단일 GitHub REST 요청을 보내고, 실패 시 응답 본문까지 포함해 에러를 올린다.
        url = f"{self.base_url}{path}"
        data = None
        headers = {
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {self.token}",
            "User-Agent": "ssd-codex-pr-review",
            "X-GitHub-Api-Version": "2022-11-28",
        }
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")
            headers["Content-Type"] = "application/json"

        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                body = response.read().decode("utf-8")
                return json.loads(body) if body else None
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"GitHub API {method} {path} failed: {error.code} {detail}") from error

    def paginate(self, path: str) -> list[dict[str, Any]]:
        # GitHub 목록 API는 페이지네이션이 있으므로 끝까지 모아서 반환한다.
        items: list[dict[str, Any]] = []
        page = 1
        while True:
            separator = "&" if "?" in path else "?"
            page_path = f"{path}{separator}per_page=100&page={page}"
            page_items = self.request("GET", page_path)
            if not page_items:
                break
            items.extend(page_items)
            if len(page_items) < 100:
                break
            page += 1
        return items

    def pull_request_files(self, number: int) -> list[dict[str, Any]]:
        # PR에 포함된 변경 파일과 patch를 가져온다.
        return self.paginate(f"/repos/{self.repo_full_name}/pulls/{number}/files")

    def issue_comments(self, number: int) -> list[dict[str, Any]]:
        # PR 대화 탭의 top-level 코멘트 목록을 가져온다.
        return self.paginate(f"/repos/{self.repo_full_name}/issues/{number}/comments")

    def create_issue_comment(self, number: int, body: str) -> None:
        # 최초 리뷰 코멘트를 생성한다.
        self.request("POST", f"/repos/{self.repo_full_name}/issues/{number}/comments", {"body": body})

    def update_issue_comment(self, comment_id: int, body: str) -> None:
        # 기존 sticky 코멘트를 최신 결과로 덮어쓴다.
        self.request("PATCH", f"/repos/{self.repo_full_name}/issues/comments/{comment_id}", {"body": body})


class OpenAIClient:
    # OpenAI Chat Completions API를 호출해 구조화된 리뷰 결과를 받는다.
    def __init__(self, api_key: str, model: str) -> None:
        self.api_key = api_key
        self.model = model or "gpt-4.1-mini"
        self.base_url = "https://api.openai.com/v1/chat/completions"

    def review(self, system_prompt: str, user_prompt: str) -> dict[str, Any]:
        # 결과를 후처리하기 쉽도록 JSON Schema로 응답 형식을 고정한다.
        payload = {
            "model": self.model,
            "temperature": 0,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "response_format": {
                "type": "json_schema",
                "json_schema": {
                    "name": "pr_review",
                    "strict": True,
                    "schema": {
                        "type": "object",
                        "additionalProperties": False,
                        "properties": {
                            "summary": {"type": "string"},
                            "findings": {
                                "type": "array",
                                "items": {
                                    "type": "object",
                                    "additionalProperties": False,
                                    "properties": {
                                        "severity": {
                                            "type": "string",
                                            "enum": ["high", "medium", "low"],
                                        },
                                        "file": {"type": "string"},
                                        "line": {"type": ["integer", "null"]},
                                        "title": {"type": "string"},
                                        "body": {"type": "string"},
                                        "confidence": {
                                            "type": "number",
                                            "minimum": 0,
                                            "maximum": 1,
                                        },
                                    },
                                    "required": [
                                        "severity",
                                        "file",
                                        "line",
                                        "title",
                                        "body",
                                        "confidence",
                                    ],
                                },
                            },
                        },
                        "required": ["summary", "findings"],
                    },
                },
            },
        }
        data = json.dumps(payload).encode("utf-8")
        request = urllib.request.Request(
            self.base_url,
            data=data,
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {self.api_key}",
            },
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=120) as response:
                body = json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"OpenAI API failed: {error.code} {detail}") from error

        choices = body.get("choices") or []
        if not choices:
            raise RuntimeError(f"OpenAI API returned no choices: {body}")
        content = choices[0].get("message", {}).get("content")
        if not content:
            raise RuntimeError(f"OpenAI API returned empty content: {body}")
        return json.loads(content)


def should_skip_file(path: str, patch: str | None) -> bool:
    # 리뷰 효율이 낮은 파일이나 patch가 없는 파일은 모델 입력에서 제외한다.
    if not patch:
        return True
    if path.endswith(tuple(SKIP_SUFFIXES)):
        return True
    if Path(path).name in SKIP_FILENAMES:
        return True
    parts = set(Path(path).parts)
    return bool(parts & SKIP_PATH_PARTS)


def truncate_text(text: str, limit: int) -> str:
    # 토큰/문자 수 제한을 넘지 않도록 긴 텍스트를 잘라낸다.
    if len(text) <= limit:
        return text
    return text[: limit - 16] + "\n...<truncated>"


def load_agent_context(changed_files: list[str]) -> str:
    # 변경 파일 기준으로 관련 AGENTS.md만 골라 프롬프트에 포함한다.
    repo_root = Path.cwd()
    agent_paths = [repo_root / "AGENTS.md"]
    top_level_dirs = {Path(path).parts[0] for path in changed_files if Path(path).parts}
    for dirname in sorted(top_level_dirs):
        candidate = repo_root / dirname / "AGENTS.md"
        if candidate.exists():
            agent_paths.append(candidate)

    chunks: list[str] = []
    total = 0
    for agent_path in agent_paths:
        text = agent_path.read_text(encoding="utf-8")
        labeled = f"[AGENTS: {agent_path.relative_to(repo_root)}]\n{text.strip()}"
        if total + len(labeled) > MAX_AGENTS_CHARS:
            remaining = max(MAX_AGENTS_CHARS - total, 0)
            if remaining <= 0:
                break
            labeled = truncate_text(labeled, remaining)
        chunks.append(labeled)
        total += len(labeled)
        if total >= MAX_AGENTS_CHARS:
            break
    return "\n\n".join(chunks)


def summarize_files(files: list[dict[str, Any]]) -> str:
    # 모델이 전체 변경 범위를 빠르게 파악하도록 파일 요약 목록을 만든다.
    lines: list[str] = []
    for file in files:
        lines.append(
            f"- {file['filename']} ({file['status']}, +{file['additions']} / -{file['deletions']})"
        )
    return "\n".join(lines)


def build_diff_payload(files: list[dict[str, Any]]) -> tuple[str, list[str], list[str]]:
    # 실제 모델에 넣을 diff 본문을 만들고, 포함/제외 파일 목록도 함께 반환한다.
    included: list[str] = []
    skipped: list[str] = []
    payload_chunks: list[str] = []
    total_chars = 0

    for file in files:
        filename = file["filename"]
        patch = file.get("patch")
        if should_skip_file(filename, patch):
            skipped.append(filename)
            continue
        if len(included) >= MAX_FILES or total_chars >= MAX_TOTAL_PATCH_CHARS:
            skipped.append(filename)
            continue

        truncated_patch = truncate_text(patch, MAX_PATCH_CHARS_PER_FILE)
        chunk = textwrap.dedent(
            f"""
            File: {filename}
            Status: {file['status']}
            Additions: {file['additions']}
            Deletions: {file['deletions']}
            ```diff
            {truncated_patch}
            ```
            """
        ).strip()
        chunk_size = len(chunk)
        if total_chars + chunk_size > MAX_TOTAL_PATCH_CHARS:
            remaining = MAX_TOTAL_PATCH_CHARS - total_chars
            if remaining < 500:
                skipped.append(filename)
                continue
            chunk = truncate_text(chunk, remaining)
            chunk_size = len(chunk)
        payload_chunks.append(chunk)
        included.append(filename)
        total_chars += chunk_size

    return "\n\n".join(payload_chunks), included, skipped


def build_prompts(pr: dict[str, Any], files: list[dict[str, Any]]) -> tuple[str, str, list[str], list[str]]:
    # PR 메타데이터, AGENTS 규칙, diff를 합쳐 최종 프롬프트를 구성한다.
    diff_payload, included, skipped = build_diff_payload(files)
    agents = load_agent_context(included or [file["filename"] for file in files])
    metadata = textwrap.dedent(
        f"""
        Repository: {pr['base']['repo']['full_name']}
        PR Number: #{pr['number']}
        Title: {pr['title']}
        Base Branch: {pr['base']['ref']}
        Head Branch: {pr['head']['ref']}
        Author: {pr['user']['login']}

        PR Body:
        {pr.get('body') or '(empty)'}
        """
    ).strip()
    changed_summary = summarize_files(files)

    system_prompt = textwrap.dedent(
        """
        You are a senior backend code reviewer.
        Review the pull request diff and report only actionable findings that are likely to cause bugs,
        regressions, security issues, data integrity problems, or important missing tests.
        Ignore style, naming, formatting, and trivial cleanup.
        Be conservative: if the evidence in the diff is weak, return no finding.
        Use the repository rules provided in AGENTS context.
        Keep findings concise and technical.
        """
    ).strip()

    user_prompt = textwrap.dedent(
        f"""
        AGENTS Context:
        {agents}

        Pull Request Metadata:
        {metadata}

        Changed Files:
        {changed_summary}

        Reviewable Diff Excerpts:
        {diff_payload or '(no reviewable diff excerpts)'}

        Output rules:
        - Return JSON only.
        - Findings must be sorted by severity and then confidence.
        - Use line=null when the diff does not support a precise line reference.
        - Do not report more than 8 findings.
        - If there are no credible findings, return an empty findings array and a short summary.
        """
    ).strip()
    return system_prompt, user_prompt, included, skipped


def format_comment(result: dict[str, Any], included: list[str], skipped: list[str], status: str = "ok") -> str:
    # 모델 결과를 사람이 읽기 쉬운 PR 코멘트 본문으로 변환한다.
    summary = result.get("summary", "") or "No summary provided."
    findings = result.get("findings", []) or []
    findings = sorted(
        findings,
        key=lambda item: (
            SEVERITY_ORDER.get(str(item.get("severity", "low")).lower(), 99),
            -float(item.get("confidence", 0)),
        ),
    )

    lines = [MARKER, COMMENT_HEADER, ""]
    if status != "ok":
        lines.append(f"Status: {status}")
        lines.append("")
    lines.append(summary)
    lines.append("")

    if findings:
        lines.append("### Findings")
        for finding in findings:
            severity = str(finding.get("severity", "low")).upper()
            file_ref = finding.get("file", "(unknown file)")
            line = finding.get("line")
            if isinstance(line, int):
                file_ref = f"{file_ref}:{line}"
            confidence = float(finding.get("confidence", 0))
            lines.append(
                f"- [{severity}] `{file_ref}` {finding.get('title', '').strip()} (confidence {confidence:.2f})"
            )
            lines.append(f"  {finding.get('body', '').strip()}")
    else:
        lines.append("### Findings")
        lines.append("- No high-confidence issues found in the provided diff.")

    lines.append("")
    lines.append("### Coverage")
    lines.append(f"- Reviewed files: {len(included)}")
    if included:
        for path in included[:10]:
            lines.append(f"- `{path}`")
        if len(included) > 10:
            lines.append(f"- ... and {len(included) - 10} more")
    if skipped:
        lines.append(f"- Skipped files: {len(skipped)}")
        for path in skipped[:10]:
            lines.append(f"- `{path}`")
        if len(skipped) > 10:
            lines.append(f"- ... and {len(skipped) - 10} more")

    body = "\n".join(lines).strip()
    return truncate_text(body, 60000)


def upsert_comment(gh: GitHubClient, pr_number: int, body: str) -> None:
    # marker가 달린 기존 코멘트가 있으면 갱신하고, 없으면 새로 만든다.
    comments = gh.issue_comments(pr_number)
    existing = next((item for item in comments if MARKER in item.get("body", "")), None)
    if existing:
        gh.update_issue_comment(existing["id"], body)
    else:
        gh.create_issue_comment(pr_number, body)


def main() -> None:
    # 전체 흐름:
    # 1) GitHub event에서 PR 정보를 읽고
    # 2) 변경 파일과 diff를 수집한 뒤
    # 3) OpenAI로 리뷰를 생성하고
    # 4) PR sticky comment를 생성/갱신한다.
    event_path = os.getenv("GITHUB_EVENT_PATH") or os.getenv("EVENT_PATH")
    github_token = os.getenv("GITHUB_TOKEN")
    if not event_path:
        fail("GITHUB_EVENT_PATH is required")
    if not github_token:
        fail("GITHUB_TOKEN is required")

    event = read_json(event_path)
    pr = event.get("pull_request")
    if not pr:
        fail("This script only supports pull_request events")

    repo_full_name = event["repository"]["full_name"]
    pr_number = int(pr["number"])
    gh = GitHubClient(repo_full_name, github_token)
    files = gh.pull_request_files(pr_number)

    # 리뷰 가능한 텍스트 diff가 하나도 없으면 스킵 코멘트만 남긴다.
    reviewable_files = [file for file in files if not should_skip_file(file["filename"], file.get("patch"))]
    if not reviewable_files:
        body = format_comment(
            {"summary": "No text diff was available for automated review.", "findings": []},
            [],
            [file["filename"] for file in files],
            status="skipped",
        )
        upsert_comment(gh, pr_number, body)
        return

    # 비밀키가 없더라도 워크플로우 전체를 실패시키지 않고 원인을 코멘트로 남긴다.
    api_key = os.getenv("OPENAI_API_KEY", "")
    if not api_key:
        body = format_comment(
            {"summary": "Automated review was skipped because `OPENAI_API_KEY` is not configured in GitHub Actions secrets.", "findings": []},
            [file["filename"] for file in reviewable_files],
            [],
            status="skipped",
        )
        upsert_comment(gh, pr_number, body)
        return

    system_prompt, user_prompt, included, skipped = build_prompts(pr, files)
    client = OpenAIClient(api_key=api_key, model=os.getenv("OPENAI_REVIEW_MODEL", "") or "gpt-4.1-mini")

    try:
        # 모델 호출 실패도 워크플로우 전체 실패보다 코멘트 가시성을 우선한다.
        result = client.review(system_prompt=system_prompt, user_prompt=user_prompt)
        body = format_comment(result, included, skipped, status="ok")
    except Exception as error:  # noqa: BLE001
        body = format_comment(
            {
                "summary": f"Automated review failed: {error}",
                "findings": [],
            },
            included,
            skipped,
            status="error",
        )
    upsert_comment(gh, pr_number, body)


if __name__ == "__main__":
    main()

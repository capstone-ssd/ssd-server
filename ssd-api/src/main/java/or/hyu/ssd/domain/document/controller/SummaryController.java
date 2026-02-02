package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.ThreeLineSummaryResponse;
import or.hyu.ssd.domain.document.service.SummaryService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "세줄요약 API", description = "문서 세 줄 요약 생성/조회/삭제")
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping("/v1/documents/{documentId}/summary/generate")
    @Operation(
            summary = "세줄요약 생성",
            description = """
                    ### 개요
                    - 문서 content를 기반으로 세 줄 요약을 생성하고 저장합니다. 기존 요약은 새로 덮어씁니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/summary/generate

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.lines[]: 생성된 세 줄 리스트

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<ThreeLineSummaryResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ThreeLineSummaryResponse dto = summaryService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "세 줄 요약이 생성되어 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/summary")
    @Operation(
            summary = "세줄요약 조회",
            description = """
                    ### 개요
                    - 문서에 저장된 세 줄 요약을 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/summary

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.lines[]: 세 줄 요약

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<ThreeLineSummaryResponse>> get(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ThreeLineSummaryResponse dto = summaryService.get(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "세 줄 요약이 조회되었습니다"));
    }

    @DeleteMapping("/v1/documents/{documentId}/summary")
    @Operation(
            summary = "세줄요약 삭제",
            description = """
                    ### 개요
                    - 문서에 저장된 세 줄 요약을 삭제합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/summary

                    ### 응답
                    - 200 OK
                    - data: "세 줄 요약이 삭제되었습니다"

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        summaryService.delete(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok("세 줄 요약이 삭제되었습니다"));
    }
}


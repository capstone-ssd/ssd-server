package or.hyu.ssd.api.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.api.document.response.DocumentLogResponse;
import or.hyu.ssd.domain.document.service.DocumentLogService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "기록 API", description = "문서 생성/수정 시 자동 생성되는 기록 조회")
public class DocumentLogController {

    private final DocumentLogService documentLogService;

    @GetMapping("/v1/documents/{documentId}/logs")
    @Operation(
            summary = "문서 기록 조회",
            description = """
                    ### 개요
                    - 문서 생성/수정 시 자동 생성된 기록을 날짜별로 조회합니다.
                    - 별도의 기록 생성/수정 API는 제공하지 않습니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/logs

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.records[].savedDate: 저장 날짜
                    - data.records[].logs[].savedTime: 저장 시간
                    - data.records[].logs[].editorName: 저장한 사람 이름
                    - data.records[].logs[].editorEmail: 저장한 사람 이메일
                    - data.records[].logs[].deletedBlockCount: 마지막 수정에서 삭제된 block 개수
                    - data.records[].logs[].createdBlockCount: 마지막 수정에서 생성된 block 개수
                    """
    )
    public ResponseEntity<ApiResponse<DocumentLogResponse>> list(
            @PathVariable @Positive(message = "문서 ID는 1 이상이어야 합니다") Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentLogResponse response = DocumentLogResponse.from(documentLogService.list(documentId, user));
        return ResponseEntity.ok(ApiResponse.ok(response, "기록이 조회되었습니다"));
    }
}

package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentDetailsResponse;
import or.hyu.ssd.domain.document.service.DetailsService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "상세요약 API", description = "문서 상세 요약 생성/조회")
public class DetailsController {

    private final DetailsService detailsService;

    @PostMapping("/v1/documents/{documentId}/details/generate")
    @Operation(
            summary = "상세요약 생성",
            description = """
                    ### 개요
                    - 문서 내용을 기반으로 상세 요약을 생성/저장합니다. 기존 상세 요약이 있으면 삭제 후 덮어씁니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/details/generate

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.details: 생성된 상세 요약

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<DocumentDetailsResponse>> generate(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentDetailsResponse dto = detailsService.generate(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "상세 요약이 생성되어 저장되었습니다"));
    }

    @GetMapping("/v1/documents/{documentId}/details")
    @Operation(
            summary = "상세요약 조회",
            description = """
                    ### 개요
                    - 문서에 저장된 상세 요약을 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (문서 작성자만)

                    ### 요청
                    - Path: /api/v1/documents/{documentId}/details

                    ### 응답
                    - 200 OK
                    - data.documentId: 문서 ID
                    - data.details: 상세 요약

                    ### 오류
                    - DOC40401: 문서를 찾을 수 없음
                    - DOC40301: 문서 소유자가 아님
                    """
    )
    public ResponseEntity<ApiResponse<DocumentDetailsResponse>> get(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DocumentDetailsResponse dto = detailsService.get(documentId, user);
        return ResponseEntity.ok(ApiResponse.ok(dto, "상세 요약이 조회되었습니다"));
    }
}

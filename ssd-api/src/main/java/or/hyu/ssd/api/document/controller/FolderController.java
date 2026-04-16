package or.hyu.ssd.api.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.api.document.request.CreateFolderRequest;
import or.hyu.ssd.api.document.response.CreateFolderResponse;
import or.hyu.ssd.api.document.response.FolderContentResponse;
import or.hyu.ssd.api.document.request.UpdateFolderRequest;
import or.hyu.ssd.api.document.response.UpdateFolderResponse;
import or.hyu.ssd.domain.document.service.FolderService;
import or.hyu.ssd.member.application.service.CustomUserDetails;
import or.hyu.ssd.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "폴더 API",
        description = "폴더 CRUD 엔드포인트"
)
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/v1/folders")
    @Operation(
            summary = "폴더 생성",
            description = """
                    ### 개요
                    - 새 폴더를 생성하고 ID를 반환합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청 본문
                    - name (string, required): 폴더명
                    - color (string, optional): 폴더 색상 (HEX 등)
                    - parentId (number, optional): 상위 폴더 ID (없으면 루트)

                    ### 응답
                    - 200 OK
                    - data.id: 생성된 폴더 ID
                    """
    )
    public ResponseEntity<ApiResponse<CreateFolderResponse>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CreateFolderResponse dto = CreateFolderResponse.from(folderService.create(user, request.toCommand()));
        return ResponseEntity.ok(ApiResponse.ok(dto, "폴더가 생성되었습니다"));
    }

    @PatchMapping("/v1/folders/{id}")
    @Operation(
            summary = "폴더 수정",
            description = """
                    ### 개요
                    - 폴더 이름/색상/위치를 수정합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (폴더 소유자만)

                    ### 요청
                    - Path: /api/v1/folders/{id}
                    - Body(JSON, required)
                      - name (string, optional): 새 폴더명
                      - color (string, optional): 새 폴더 색상
                      - parentId (number, optional): 상위 폴더 ID (0이면 루트로 이동)

                    ### 응답
                    - 200 OK
                    - data.id: 수정된 폴더 ID
                    """
    )
    public ResponseEntity<ApiResponse<UpdateFolderResponse>> updateFolder(
            @Parameter(description = "수정할 폴더 ID", example = "42")
            @PathVariable("id") @Positive(message = "폴더 ID는 1 이상이어야 합니다") Long id,
            @Valid @RequestBody UpdateFolderRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateFolderResponse dto = UpdateFolderResponse.from(folderService.update(id, user, request.toCommand()));
        return ResponseEntity.ok(ApiResponse.ok(dto, "폴더가 수정되었습니다"));
    }

    @DeleteMapping("/v1/folders/{id}")
    @Operation(
            summary = "폴더 삭제",
            description = """
                    ### 개요
                    - 폴더와 하위 폴더, 포함된 문서를 모두 삭제합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken} (폴더 소유자만)

                    ### 요청
                    - Path: /api/v1/folders/{id}

                    ### 응답
                    - 200 OK
                    - data: "폴더가 삭제되었습니다"
                    """
    )
    public ResponseEntity<ApiResponse<String>> deleteFolder(
            @Parameter(description = "삭제할 폴더 ID", example = "42")
            @PathVariable("id") @Positive(message = "폴더 ID는 1 이상이어야 합니다") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        folderService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.ok("폴더가 삭제되었습니다"));
    }

    @GetMapping("/v1/folders")
    @Operation(
            summary = "폴더 목록 조회",
            description = """
                    ### 개요
                    - 특정 부모 폴더의 하위 폴더와 문서를 함께 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Query parentId (optional): 부모 폴더 ID (없으면 루트)

                    ### 응답
                    - 200 OK
                    - data[]
                      - parentId: 현재 폴더의 상위 폴더 ID (루트는 0)
                      - currentFolderId: 현재 조회 중인 폴더 ID (루트는 0)
                      - folders[]: 하위 폴더 목록
                      - documents[]: 해당 폴더 내부 문서 목록
                    """
    )
    public ResponseEntity<ApiResponse<FolderContentResponse>> listFolders(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "부모 폴더 ID (없으면 루트)", schema = @Schema(type = "integer", example = "0"))
            @RequestParam(name = "parentId", required = false) @PositiveOrZero(message = "parentId는 0 이상이어야 합니다") Long parentId
    ) {
        FolderContentResponse data = FolderContentResponse.from(folderService.listContent(user, parentId));
        return ResponseEntity.ok(ApiResponse.ok(data, "폴더 내용이 조회되었습니다"));
    }

    @GetMapping("/v1/folders/all")
    @Operation(
            summary = "파일 경로 전체 조회",
            description = """
                    ### 개요
                    - 로그인한 회원이 보유한 모든 폴더/문서를 한 번에 조회합니다.
                    - 응답의 folders는 각 항목 parentId로 계층을 복원할 수 있습니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 응답
                    - 200 OK
                    - data
                      - parentId: 0 (루트 기준)
                      - currentFolderId: 0 (루트 기준)
                      - folders[]: 전체 폴더 목록
                      - documents[]: 전체 문서 목록
                    """
    )
    public ResponseEntity<ApiResponse<FolderContentResponse>> listAllPaths(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FolderContentResponse data = FolderContentResponse.from(folderService.listAllContent(user));
        return ResponseEntity.ok(ApiResponse.ok(data, "파일 경로 전체 조회에 성공했습니다"));
    }
}

package or.hyu.ssd.domain.document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CreateFolderRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateFolderResponse;
import or.hyu.ssd.domain.document.controller.dto.FolderListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateFolderRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateFolderResponse;
import or.hyu.ssd.domain.document.service.FolderService;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
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
        CreateFolderResponse dto = folderService.create(user, request);
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
            @PathVariable("id") Long id,
            @RequestBody UpdateFolderRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        UpdateFolderResponse dto = folderService.update(id, user, request);
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
            @PathVariable("id") Long id,
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
                    - 특정 부모 폴더의 하위 폴더 목록을 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 요청
                    - Query parentId (optional): 상위 폴더 ID (없으면 루트)

                    ### 응답
                    - 200 OK
                    - data[]
                      - id: 폴더 ID
                      - name: 폴더명
                      - color: 폴더 색상
                      - parentId: 상위 폴더 ID
                      - hasChildren: 하위 폴더 존재 여부
                      - updatedAt: 마지막 수정 시각
                    """
    )
    public ResponseEntity<ApiResponse<List<FolderListItemResponse>>> listFolders(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "상위 폴더 ID (없으면 루트)", schema = @Schema(type = "integer", example = "0"))
            @RequestParam(name = "parentId", required = false) Long parentId
    ) {
        List<FolderListItemResponse> list = folderService.list(user, parentId);
        return ResponseEntity.ok(ApiResponse.ok(list, "폴더 목록이 조회되었습니다"));
    }
}

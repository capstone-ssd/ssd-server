package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CreateFolderRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateFolderResponse;
import or.hyu.ssd.domain.document.controller.dto.FolderListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateFolderRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateFolderResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.Folder;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.FolderRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    public CreateFolderResponse create(CustomUserDetails user, CreateFolderRequest request) {
        ensureAuthenticated(user);

        String name = request.name().trim();
        String color = trimOrNull(request.color());
        Folder parent = resolveParent(user, request.parentId());

        Folder saved = folderRepository.save(Folder.of(name, color, parent, user.getMember()));
        return CreateFolderResponse.of(saved.getId());
    }

    public UpdateFolderResponse update(Long folderId, CustomUserDetails user, UpdateFolderRequest request) {
        Folder folder = getFolderOwned(folderId, user);

        String name = trimOrNull(request.name());
        String color = trimOrNull(request.color());
        if (name != null || color != null) {
            folder.updateIfPresent(name, color, null);
        }

        if (request.parentId() != null) {
            if (request.parentId() == 0L) {
                folder.updateParent(null);
            } else {
                Folder newParent = getFolderOwned(request.parentId(), user);
                ensureMovable(folder, newParent);
                folder.updateParent(newParent);
            }
        }

        return UpdateFolderResponse.of(folder.getId());
    }

    public void delete(Long folderId, CustomUserDetails user) {
        Folder folder = getFolderOwned(folderId, user);
        deleteRecursively(folder, user);
    }

    @Transactional(readOnly = true)
    public List<FolderListItemResponse> list(CustomUserDetails user, Long parentId) {
        ensureAuthenticated(user);
        Long memberId = user.getMember().getId();

        List<Folder> folders;
        if (parentId == null || parentId == 0L) {
            folders = folderRepository.findAllByMember_IdAndParentIsNull(memberId);
        } else {
            getFolderOwned(parentId, user);
            folders = folderRepository.findAllByMember_IdAndParent_Id(memberId, parentId);
        }

        return folders.stream()
                .map(folder -> FolderListItemResponse.of(
                        folder,
                        folderRepository.existsByMember_IdAndParent_Id(memberId, folder.getId())
                ))
                .collect(Collectors.toList());
    }

    private void deleteRecursively(Folder folder, CustomUserDetails user) {
        List<Document> documents = documentRepository.findAllByFolder_Id(folder.getId());
        for (Document document : documents) {
            documentService.deleteDocument(document.getId(), user);
        }

        List<Folder> children = folderRepository.findAllByMember_IdAndParent_Id(
                folder.getMember().getId(),
                folder.getId()
        );
        for (Folder child : children) {
            deleteRecursively(child, user);
        }

        folderRepository.delete(folder);
    }

    private Folder resolveParent(CustomUserDetails user, Long parentId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }
        return getFolderOwned(parentId, user);
    }

    private Folder getFolderOwned(Long folderId, CustomUserDetails user) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.FOLDER_NOT_FOUND));
        ensureOwner(folder, user);
        return folder;
    }

    private void ensureOwner(Folder folder, CustomUserDetails user) {
        if (folder.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.FOLDER_FORBIDDEN);
        }
    }

    private void ensureAuthenticated(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void ensureMovable(Folder folder, Folder newParent) {
        if (folder.getId().equals(newParent.getId())) {
            throw new UserExceptionHandler(ErrorCode.FOLDER_INVALID_PARENT);
        }
        Folder cursor = newParent;
        while (cursor != null) {
            if (cursor.getId().equals(folder.getId())) {
                throw new UserExceptionHandler(ErrorCode.FOLDER_INVALID_PARENT);
            }
            cursor = cursor.getParent();
        }
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

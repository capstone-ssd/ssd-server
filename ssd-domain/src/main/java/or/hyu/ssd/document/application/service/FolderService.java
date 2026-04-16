package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.Folder;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.command.CreateFolderCommand;
import or.hyu.ssd.document.application.command.UpdateFolderCommand;
import or.hyu.ssd.document.application.result.CreateFolderResult;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.application.result.FolderContentResult;
import or.hyu.ssd.document.application.result.FolderListItemResult;
import or.hyu.ssd.document.application.result.UpdateFolderResult;
import or.hyu.ssd.member.application.service.CustomUserDetails;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.data.domain.Sort;
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
    private final DocumentCommandService documentCommandService;

    public CreateFolderResult create(CustomUserDetails user, CreateFolderCommand command) {
        validateCreateRequest(command);
        ensureAuthenticated(user);

        String name = command.name().trim();
        String color = trimOrNull(command.color());
        Folder parent = resolveParent(user, command.parentId());

        Folder saved = folderRepository.save(Folder.of(name, color, parent, user.getMember()));
        return CreateFolderResult.of(saved.getId());
    }

    public UpdateFolderResult update(Long folderId, CustomUserDetails user, UpdateFolderCommand command) {
        Folder folder = getFolderOwned(folderId, user);
        validateUpdateRequest(command);

        String name = trimOrNull(command.name());
        String color = trimOrNull(command.color());
        if (name != null || color != null) {
            folder.updateIfPresent(name, color, null);
        }

        if (command.parentId() != null) {
            if (command.parentId() == 0L) {
                folder.updateParent(null);
            } else {
                Folder newParent = getFolderOwned(command.parentId(), user);
                ensureMovable(folder, newParent);
                folder.updateParent(newParent);
            }
        }

        return UpdateFolderResult.of(folder.getId());
    }

    public void delete(Long folderId, CustomUserDetails user) {
        Folder folder = getFolderOwned(folderId, user);
        deleteRecursively(folder, user);
    }

    @Transactional(readOnly = true)
    public FolderContentResult listContent(CustomUserDetails user, Long parentId) {
        ensureAuthenticated(user);
        Long memberId = user.getMember().getId();
        Long requestedFolderId = (parentId == null) ? 0L : parentId;
        Long currentFolderId = 0L;
        Long parentFolderId = 0L;

        List<Folder> folders;
        List<Document> documents;
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));

        if (requestedFolderId == 0L) {
            folders = folderRepository.findAllByMember_IdAndParentIsNull(memberId);
            documents = documentRepository.findAllByMember_IdAndFolderIsNull(memberId, sort);
        } else {
            Folder currentFolder = getFolderOwned(requestedFolderId, user);
            currentFolderId = currentFolder.getId();
            parentFolderId = currentFolder.getParent() == null ? 0L : currentFolder.getParent().getId();
            folders = folderRepository.findAllByMember_IdAndParent_Id(memberId, requestedFolderId);
            documents = documentRepository.findAllByMember_IdAndFolder_Id(memberId, requestedFolderId, sort);
        }

        return FolderContentResult.of(
                parentFolderId,
                currentFolderId,
                toFolderItems(memberId, folders),
                toDocumentItems(documents)
        );
    }

    @Transactional(readOnly = true)
    public FolderContentResult listAllContent(CustomUserDetails user) {
        ensureAuthenticated(user);
        Long memberId = user.getMember().getId();
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));

        List<Folder> folders = folderRepository.findAllByMember_Id(memberId, sort);
        List<Document> documents = documentRepository.findAllByMember_Id(memberId, sort);

        return FolderContentResult.of(
                0L,
                0L,
                toFolderItems(memberId, folders),
                toDocumentItems(documents)
        );
    }

    private void deleteRecursively(Folder folder, CustomUserDetails user) {
        List<Document> documents = documentRepository.findAllByFolder_Id(folder.getId());
        for (Document document : documents) {
            documentCommandService.deleteDocument(document.getId(), user);
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
                .orElseThrow(() -> new DocumentException(ErrorCode.FOLDER_NOT_FOUND));
        ensureOwner(folder, user);
        return folder;
    }

    private void ensureOwner(Folder folder, CustomUserDetails user) {
        if (folder.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
    }

    private void ensureAuthenticated(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void ensureMovable(Folder folder, Folder newParent) {
        if (folder.getId().equals(newParent.getId())) {
            throw new DocumentException(ErrorCode.FOLDER_INVALID_PARENT);
        }
        Folder cursor = newParent;
        while (cursor != null) {
            if (cursor.getId().equals(folder.getId())) {
                throw new DocumentException(ErrorCode.FOLDER_INVALID_PARENT);
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

    private void validateCreateRequest(CreateFolderCommand command) {
        if (command == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "생성 요청 본문이 비어 있습니다");
        }
        if (command.name() == null || command.name().trim().isEmpty()) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "폴더명은 공백일 수 없습니다");
        }
        validateParentId(command.parentId());
    }

    private void validateUpdateRequest(UpdateFolderCommand command) {
        if (command == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청 본문이 비어 있습니다");
        }
        if (command.name() != null && command.name().trim().isEmpty()) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "폴더명은 공백일 수 없습니다");
        }
        validateParentId(command.parentId());
        if (command.name() == null && command.color() == null && command.parentId() == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정할 값을 하나 이상 입력해 주세요");
        }
    }

    private void validateParentId(Long parentId) {
        if (parentId != null && parentId < 0L) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "상위 폴더 ID는 0 이상이어야 합니다");
        }
    }

    private List<FolderListItemResult> toFolderItems(Long memberId, List<Folder> folders) {
        return folders.stream()
                .map(folder -> FolderListItemResult.of(
                        folder,
                        folderRepository.existsByMember_IdAndParent_Id(memberId, folder.getId())
                ))
                .collect(Collectors.toList());
    }

    private List<DocumentListItemResult> toDocumentItems(List<Document> documents) {
        return documents.stream()
                .map(DocumentListItemResult::of)
                .collect(Collectors.toList());
    }
}

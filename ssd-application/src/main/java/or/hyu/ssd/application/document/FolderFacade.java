package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.CreateFolderCommand;
import or.hyu.ssd.document.application.command.UpdateFolderCommand;
import or.hyu.ssd.document.application.result.CreateFolderResult;
import or.hyu.ssd.document.application.result.FolderContentResult;
import or.hyu.ssd.document.application.result.UpdateFolderResult;
import or.hyu.ssd.document.application.service.FolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderFacade {

    private final FolderService folderService;

    @Transactional
    public CreateFolderResult create(Long memberId, CreateFolderCommand command) {
        return folderService.create(memberId, command);
    }

    @Transactional
    public UpdateFolderResult update(Long folderId, Long memberId, UpdateFolderCommand command) {
        return folderService.update(folderId, memberId, command);
    }

    @Transactional
    public void delete(Long folderId, Long memberId) {
        folderService.delete(folderId, memberId);
    }

    @Transactional(readOnly = true)
    public FolderContentResult listContent(Long memberId, Long parentId) {
        return folderService.listContent(memberId, parentId);
    }

    @Transactional(readOnly = true)
    public FolderContentResult listAllContent(Long memberId) {
        return folderService.listAllContent(memberId);
    }
}

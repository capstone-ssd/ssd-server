package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.AssertTrue;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.usecase.command.DocumentBlockCommand;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public record CreateDocumentBlockRequest(
        DocumentBlockType type,
        String content,
        String role,
        Integer blockId,
        String blobKey,
        String url
) {
    private static final Pattern ROLE_PATTERN = Pattern.compile("^#{0,6}$");

    public DocumentBlockType resolvedType() {
        return type == null ? DocumentBlockType.PARAGRAPH : type;
    }

    @AssertTrue(message = "문단 블록은 content와 role이 필요하고, 이미지 블록은 blobKey 또는 url이 필요합니다")
    public boolean isValidBlockStructure() {
        if (resolvedType().isParagraph()) {
            return StringUtils.hasText(content)
                    && role != null
                    && ROLE_PATTERN.matcher(role).matches()
                    && !StringUtils.hasText(blobKey)
                    && !StringUtils.hasText(url);
        }

        return !StringUtils.hasText(content)
                && !StringUtils.hasText(role)
                && (StringUtils.hasText(blobKey) || StringUtils.hasText(url));
    }

    public DocumentBlockCommand toCommand() {
        return new DocumentBlockCommand(resolvedType(), content, role, blockId, blobKey, url);
    }
}

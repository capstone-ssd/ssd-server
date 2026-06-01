package or.hyu.ssd.api.document.request;

import jakarta.validation.constraints.AssertTrue;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.document.application.command.DocumentBlockCommand;
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
        String sanitizedRole = RequestStringSanitizer.stripNullChar(role);

        if (resolvedType().isParagraph()) {
            return StringUtils.hasText(content)
                    && sanitizedRole != null
                    && ROLE_PATTERN.matcher(sanitizedRole).matches()
                    && !StringUtils.hasText(blobKey)
                    && !StringUtils.hasText(url);
        }

        return !StringUtils.hasText(content)
                && !StringUtils.hasText(sanitizedRole)
                && (StringUtils.hasText(blobKey) || StringUtils.hasText(url));
    }

    public DocumentBlockCommand toCommand() {
        return new DocumentBlockCommand(
                resolvedType(),
                RequestStringSanitizer.stripNullChar(content),
                RequestStringSanitizer.stripNullChar(role),
                blockId,
                RequestStringSanitizer.stripNullChar(blobKey),
                RequestStringSanitizer.stripNullChar(url)
        );
    }
}

package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentParagraphValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    @DisplayName("문단 블록은 content와 role 조합을 검증한다")
    void createDocumentParagraphRoleValidation() {
        CreateDocumentParagraphRequest validEmpty = new CreateDocumentParagraphRequest(DocumentBlockType.PARAGRAPH, "본문", "", 1, null, null);
        CreateDocumentParagraphRequest validHeading = new CreateDocumentParagraphRequest(DocumentBlockType.PARAGRAPH, "본문", "#####", 1, null, null);
        CreateDocumentParagraphRequest invalidBody = new CreateDocumentParagraphRequest(DocumentBlockType.PARAGRAPH, "본문", "BODY", 1, null, null);

        assertThat(VALIDATOR.validate(validEmpty)).isEmpty();
        assertThat(VALIDATOR.validate(validHeading)).isEmpty();
        assertThat(extractMessages(VALIDATOR.validate(invalidBody)))
                .contains("문단 블록은 content와 role이 필요하고, 이미지 블록은 blobKey 또는 url이 필요합니다");
    }

    @Test
    @DisplayName("이미지 블록은 blobKey 또는 url이 있어야 한다")
    void imageBlockValidation() {
        CreateDocumentParagraphRequest validBlobKey = new CreateDocumentParagraphRequest(DocumentBlockType.IMAGE, null, null, 2, "img-1", null);
        CreateDocumentParagraphRequest validUrl = new CreateDocumentParagraphRequest(DocumentBlockType.IMAGE, null, null, 2, null, "https://example.com/image.png");
        CreateDocumentParagraphRequest invalidImage = new CreateDocumentParagraphRequest(DocumentBlockType.IMAGE, null, null, 2, null, null);

        assertThat(VALIDATOR.validate(validBlobKey)).isEmpty();
        assertThat(VALIDATOR.validate(validUrl)).isEmpty();
        assertThat(extractMessages(VALIDATOR.validate(invalidImage)))
                .contains("문단 블록은 content와 role이 필요하고, 이미지 블록은 blobKey 또는 url이 필요합니다");
    }

    private static Set<String> extractMessages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}

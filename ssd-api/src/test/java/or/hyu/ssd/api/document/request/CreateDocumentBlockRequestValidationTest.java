package or.hyu.ssd.api.document.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.document.domain.entity.DocumentBlockType;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDocumentBlockRequestValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    @DisplayName("문단 블록은 content와 role 조합을 검증한다")
    void createDocumentBlockRoleValidation() {
        // given
        CreateDocumentBlockRequest validEmpty = new CreateDocumentBlockRequest(DocumentBlockType.PARAGRAPH, "본문", "", 1, null, null);
        CreateDocumentBlockRequest validHeading = new CreateDocumentBlockRequest(DocumentBlockType.PARAGRAPH, "본문", "#####", 1, null, null);
        CreateDocumentBlockRequest invalidBody = new CreateDocumentBlockRequest(DocumentBlockType.PARAGRAPH, "본문", "BODY", 1, null, null);

        // when
        Set<String> invalidMessages = extractMessages(VALIDATOR.validate(invalidBody));

        // then
        assertThat(VALIDATOR.validate(validEmpty)).isEmpty();
        assertThat(VALIDATOR.validate(validHeading)).isEmpty();
        assertThat(invalidMessages)
                .contains("문단 블록은 content와 role이 필요하고, 이미지 블록은 blobKey 또는 url이 필요합니다");
    }

    @Test
    @DisplayName("이미지 블록은 blobKey 또는 url이 있어야 한다")
    void imageBlockValidation() {
        // given
        CreateDocumentBlockRequest validBlobKey = new CreateDocumentBlockRequest(DocumentBlockType.IMAGE, null, null, 2, "img-1", null);
        CreateDocumentBlockRequest validUrl = new CreateDocumentBlockRequest(DocumentBlockType.IMAGE, null, null, 2, null, "https://example.com/image.png");
        CreateDocumentBlockRequest invalidImage = new CreateDocumentBlockRequest(DocumentBlockType.IMAGE, null, null, 2, null, null);

        // when
        Set<String> invalidMessages = extractMessages(VALIDATOR.validate(invalidImage));

        // then
        assertThat(VALIDATOR.validate(validBlobKey)).isEmpty();
        assertThat(VALIDATOR.validate(validUrl)).isEmpty();
        assertThat(invalidMessages)
                .contains("문단 블록은 content와 role이 필요하고, 이미지 블록은 blobKey 또는 url이 필요합니다");
    }

    private static Set<String> extractMessages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}

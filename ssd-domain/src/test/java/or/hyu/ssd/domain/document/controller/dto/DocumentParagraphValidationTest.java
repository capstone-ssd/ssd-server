package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("생성 문단 role은 빈 문자열과 1~6개의 # 만 허용한다")
    void createDocumentParagraphRoleValidation() {
        CreateDocumentParagraphRequest validEmpty = new CreateDocumentParagraphRequest("본문", "", 1);
        CreateDocumentParagraphRequest validHeading = new CreateDocumentParagraphRequest("본문", "#####", 1);
        CreateDocumentParagraphRequest invalidBody = new CreateDocumentParagraphRequest("본문", "BODY", 1);

        assertThat(VALIDATOR.validate(validEmpty)).isEmpty();
        assertThat(VALIDATOR.validate(validHeading)).isEmpty();
        assertThat(extractMessages(VALIDATOR.validate(invalidBody)))
                .contains("문단 역할은 '', '#', '##', '###', '####', '#####', '######' 중 하나여야 합니다");
    }

    @Test
    @DisplayName("수정 문단 role도 동일한 고정값만 허용한다")
    void updateDocumentParagraphRoleValidation() {
        DocumentParagraphDto validEmpty = new DocumentParagraphDto("본문", "", 1, 1);
        DocumentParagraphDto invalidRole = new DocumentParagraphDto("본문", "BODY", 1, 1);

        assertThat(VALIDATOR.validate(validEmpty)).isEmpty();
        assertThat(extractMessages(VALIDATOR.validate(invalidRole)))
                .contains("문단 역할은 '', '#', '##', '###', '####', '#####', '######' 중 하나여야 합니다");
    }

    private static Set<String> extractMessages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}

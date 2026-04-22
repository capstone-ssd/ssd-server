package or.hyu.ssd.api.document.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDocumentRequestValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    @DisplayName("문단 블록 목록은 null 항목을 허용하지 않는다")
    void paragraphsRejectNullItem() {
        // given
        CreateDocumentRequest request = new CreateDocumentRequest(
                "제목",
                "본문",
                Arrays.asList(new CreateDocumentBlockRequest(null, "본문", "", 1, null, null), null),
                0L,
                DocumentPurpose.WRITING
        );

        // when
        Set<String> messages = extractMessages(VALIDATOR.validate(request));

        // then
        assertThat(messages)
                .contains("문단 블록 항목은 null일 수 없습니다");
    }

    @Test
    @DisplayName("문서 목적은 null을 허용하지 않는다")
    void purposeRejectsNull() {
        // given
        CreateDocumentRequest request = new CreateDocumentRequest(
                "제목",
                "본문",
                Arrays.asList(new CreateDocumentBlockRequest(null, "본문", "", 1, null, null)),
                0L,
                null
        );

        // when
        Set<String> messages = extractMessages(VALIDATOR.validate(request));

        // then
        assertThat(messages)
                .contains("문서 목적은 필수입니다");
    }

    private static Set<String> extractMessages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}

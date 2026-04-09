package or.hyu.ssd.domain.document.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateEvaluatorReviewCommandTest {

    @Test
    @DisplayName("필수 점수 중 하나라도 null이면 커맨드 생성이 실패한다")
    void constructorRejectsNullScore() {
        assertThatThrownBy(() -> new UpdateEvaluatorReviewCommand(null, 80, 70, "의견"))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("리뷰 점수는 모두 필수입니다");
    }
}

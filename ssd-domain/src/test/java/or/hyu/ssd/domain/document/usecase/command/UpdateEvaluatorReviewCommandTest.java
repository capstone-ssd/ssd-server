package or.hyu.ssd.domain.document.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateEvaluatorReviewCommandTest {

    @ParameterizedTest(name = "[{index}] feasibility={0}, differentiation={1}, financial={2}")
    @MethodSource("nullScoreCases")
    @DisplayName("필수 점수 중 하나라도 null이면 커맨드 생성이 실패한다")
    void constructorRejectsNullScore(Integer feasibility, Integer differentiation, Integer financial) {
        // given

        // when

        // then
        assertThatThrownBy(() -> new UpdateEvaluatorReviewCommand(feasibility, differentiation, financial, "의견"))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("리뷰 점수는 모두 필수입니다");
    }

    private static Stream<Arguments> nullScoreCases() {
        return Stream.of(
                Arguments.of(null, 80, 70),
                Arguments.of(90, null, 70),
                Arguments.of(90, 80, null)
        );
    }
}

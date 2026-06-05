package or.hyu.ssd.api.document.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.document.application.result.EvaluatorReviewDetailResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListItemResult;
import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluatorReviewResponseRoundingTest {

    @Test
    @DisplayName("리뷰 상세 응답 총점은 반올림된 정수로 변환된다")
    void detailResponseRoundsScore() {
        // given
        EvaluatorReviewDetailResult result = new EvaluatorReviewDetailResult(
                1L,
                "리뷰어",
                "reviewer@example.com",
                LocalDateTime.of(2026, 6, 5, 12, 0),
                8,
                8,
                9,
                8.333333,
                "의견"
        );

        // when
        EvaluatorReviewDetailResponse response = EvaluatorReviewDetailResponse.from(result);

        // then
        assertThat(response.totalScore()).isEqualTo(8);
    }

    @Test
    @DisplayName("리뷰 목록 응답 점수들은 반올림된 정수로 변환된다")
    void listResponseRoundsScores() {
        // given
        EvaluatorReviewListItemResult item = new EvaluatorReviewListItemResult(
                1L,
                "리뷰어",
                "reviewer@example.com",
                LocalDateTime.of(2026, 6, 5, 12, 0),
                8,
                8,
                9,
                8.666667,
                "의견"
        );
        EvaluatorReviewListResult result = EvaluatorReviewListResult.of(10L, 8.333333, 1, List.of(item));

        // when
        EvaluatorReviewListResponse response = EvaluatorReviewListResponse.from(result);

        // then
        assertThat(response.averageTotalScore()).isEqualTo(8);
        assertThat(response.reviews()).hasSize(1);
        assertThat(response.reviews().getFirst().totalScore()).isEqualTo(9);
    }
}

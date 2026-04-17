package or.hyu.ssd.document.domain.entity;

import lombok.*;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class EvaluatorReview extends AuditableDomainEntity {

    private Long id;

    private int scoreFeasibility;

    private int scoreDifferentiation;

    private int scoreFinancial;

    private String comment;

    private double scoreTotal;

    private Document document;

    private Member reviewer;

    private Long version;

    public static EvaluatorReview of(int feasibility, int differentiation, int financial, String comment, Document doc, Member reviewer) {
        return EvaluatorReview.builder()
                .scoreFeasibility(feasibility)
                .scoreDifferentiation(differentiation)
                .scoreFinancial(financial)
                .comment(comment)
                .scoreTotal(average(feasibility, differentiation, financial))
                .document(doc)
                .reviewer(reviewer)
                .build();
    }

    public void updateScores(int feasibility, int differentiation, int financial, String comment) {
        this.scoreFeasibility = feasibility;
        this.scoreDifferentiation = differentiation;
        this.scoreFinancial = financial;
        this.comment = comment;
        this.scoreTotal = average(feasibility, differentiation, financial);
    }

    private static double average(int feasibility, int differentiation, int financial) {
        return (feasibility + differentiation + financial) / 3.0;
    }
}

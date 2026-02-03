package or.hyu.ssd.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "evaluator_reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_evaluator_review_doc_member", columnNames = {"document_id", "member_id"})
        },
        indexes = {
                @Index(name = "idx_evaluator_review_document_id", columnList = "document_id"),
                @Index(name = "idx_evaluator_review_member_id", columnList = "member_id")
        }
)
@Comment("평가자 리뷰 엔티티")
public class EvaluatorReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사업타당성 점수 (0~100)")
    @Column(name = "score_feasibility", nullable = false)
    private int scoreFeasibility;

    @Comment("사업차별성 점수 (0~100)")
    @Column(name = "score_differentiation", nullable = false)
    private int scoreDifferentiation;

    @Comment("재무적정성 점수 (0~100)")
    @Column(name = "score_financial", nullable = false)
    private int scoreFinancial;

    @Comment("평가자 코멘트")
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Comment("세 항목 평균 점수 (0~100)")
    @Column(name = "score_total", nullable = false)
    private double scoreTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member reviewer;

    @Version
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

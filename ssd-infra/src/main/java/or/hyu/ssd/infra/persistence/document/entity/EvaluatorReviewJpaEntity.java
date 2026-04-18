package or.hyu.ssd.infra.persistence.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
public class EvaluatorReviewJpaEntity extends BaseJpaEntity {

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
    private DocumentJpaEntity document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberJpaEntity reviewer;

    @Version
    private Long version;

    public static EvaluatorReviewJpaEntity of(
            int feasibility,
            int differentiation,
            int financial,
            String comment,
            DocumentJpaEntity document,
            MemberJpaEntity reviewer
    ) {
        return EvaluatorReviewJpaEntity.builder()
                .scoreFeasibility(feasibility)
                .scoreDifferentiation(differentiation)
                .scoreFinancial(financial)
                .comment(comment)
                .scoreTotal(average(feasibility, differentiation, financial))
                .document(document)
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

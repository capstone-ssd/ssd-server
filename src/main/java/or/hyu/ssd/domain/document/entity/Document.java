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
@Table(name = "documents")
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사업계획서 제목")
    @Column(name = "title", nullable = false)
    private String title;

    @Comment("사업계획서 본문")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("사업계획서 즐겨찾기 여부")
    @Column(name = "bookmark", nullable = false)
    private boolean bookmark;

    @Comment("ai가 생성한 사업계획서 세 줄 요약")
    @Column(name = "summary", nullable = true, columnDefinition = "TEXT")
    private String summary;

    @Comment("ai가 생성한 사업계획서 상세 요약")
    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    @Comment("ai가 생성한 사업계획서 상세 평가")
    @Column(name = "evaluation", nullable = true, columnDefinition = "TEXT")
    private String evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Comment("평가자 리뷰 - 사업타당성 평균 점수")
    @Column(name = "review_feasibility_avg")
    private Double reviewFeasibilityAvg;

    @Comment("평가자 리뷰 - 사업차별성 평균 점수")
    @Column(name = "review_differentiation_avg")
    private Double reviewDifferentiationAvg;

    @Comment("평가자 리뷰 - 재무적정성 평균 점수")
    @Column(name = "review_financial_avg")
    private Double reviewFinancialAvg;

    @Comment("평가자 리뷰 - 전체 평균 점수")
    @Column(name = "review_total_avg")
    private Double reviewTotalAvg;

    @Comment("평가자 리뷰 - 참여 평가자 수")
    @Column(name = "review_count")
    private Integer reviewCount;

    @Version
    private Long version;



    /**
     * 수정기록과 주석 관련 기능 확인하고 ERD 반영해야함 (11/20)
     * */



    public static Document of(String title, String content, boolean bookmark, Member member) {
        return Document.builder()
                .title(title)
                .content(content)
                .bookmark(bookmark)
                .member(member)
                .build();
    }

    // 부분/전체 수정 편의 메서드
    public void updateIfPresent(String title, String content, String summary, String details, Boolean bookmark) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (summary != null) this.summary = summary;
        if (details != null) this.details = details;
        if (bookmark != null) this.bookmark = bookmark;
    }

    public void updateEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public void updateDetails(String details) {
        this.details = details;
    }

    public void updateReviewSummary(Double feasibilityAvg, Double differentiationAvg, Double financialAvg, Double totalAvg, Integer count) {
        this.reviewFeasibilityAvg = feasibilityAvg;
        this.reviewDifferentiationAvg = differentiationAvg;
        this.reviewFinancialAvg = financialAvg;
        this.reviewTotalAvg = totalAvg;
        this.reviewCount = count;
    }
}

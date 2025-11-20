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

    @Comment("사업계획서 본문")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("사업계획서 즐겨찾기 여부")
    @Column(name = "bookmark", nullable = false)
    private Boolean bookmark;

    @Comment("ai가 생성한 사업계획서 세 줄 요약")
    @Column(name = "summary", nullable = true, columnDefinition = "TEXT")
    private String summary;

    @Comment("ai가 생성한 사업계획서 상세 요약")
    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;



    /**
     * 수정기록과 주석 관련 기능 확인하고 ERD 반영해야함 (11/20)
     * */
}

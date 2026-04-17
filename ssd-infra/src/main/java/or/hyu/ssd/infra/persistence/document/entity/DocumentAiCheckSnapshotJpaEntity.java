
package or.hyu.ssd.infra.persistence.document.entity;











import jakarta.persistence.*;



import lombok.*;



import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;



import org.hibernate.annotations.Comment;







@Entity



@Getter
@Setter



@Builder



@NoArgsConstructor



@AllArgsConstructor



@Table(



        name = "document_ai_check_snapshots",



        uniqueConstraints = {



                @UniqueConstraint(name = "uk_document_ai_check_snapshot_doc_block", columnNames = {"document_id", "block_id"})



        },



        indexes = {



                @Index(name = "idx_document_ai_check_snapshot_document_id", columnList = "document_id")



        }



)



@Comment("외부 AI 체크리스트 비교용 문단 스냅샷")



public class DocumentAiCheckSnapshotJpaEntity extends BaseJpaEntity {







    @Id



    @GeneratedValue(strategy = GenerationType.IDENTITY)



    private Long id;







    @ManyToOne(fetch = FetchType.LAZY)



    @JoinColumn(name = "document_id", nullable = false)



    private DocumentJpaEntity document;







    @Comment("문단 블록 ID")



    @Column(name = "block_id", nullable = false)



    private int blockId;







    @Comment("AI 체크 기준 문단 본문")



    @Column(name = "content", nullable = false, columnDefinition = "TEXT")



    private String content;







    public static DocumentAiCheckSnapshotJpaEntity of(DocumentJpaEntity document, int blockId, String content) {



        return DocumentAiCheckSnapshotJpaEntity.builder()



                .document(document)



                .blockId(blockId)



                .content(content)



                .build();



    }



}




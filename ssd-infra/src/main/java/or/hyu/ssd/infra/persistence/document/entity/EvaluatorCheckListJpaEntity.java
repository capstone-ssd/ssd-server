
package or.hyu.ssd.infra.persistence.document.entity;











import jakarta.persistence.*;



import lombok.*;



import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;



import org.hibernate.annotations.Comment;







@Entity



@NoArgsConstructor



@AllArgsConstructor



@Getter
@Setter



@Builder



@Table(



        name = "evaluator_check_lists",



        uniqueConstraints = {



                @UniqueConstraint(name = "uk_evaluator_checklist_doc_content", columnNames = {"document_id", "content"})



        },



        indexes = {



                @Index(name = "idx_evaluator_checklist_document_id", columnList = "document_id")



        }



)



@Comment("평가자 체크리스트")



public class EvaluatorCheckListJpaEntity extends BaseJpaEntity {







    @Id



    @GeneratedValue(strategy = GenerationType.IDENTITY)



    private Long id;







    @Comment("평가자 체크리스트 본문")



    @Column(name = "content", nullable = false)



    private String content;







    @Comment("AI가 판단한 충족 여부")



    @Column(name = "checked", nullable = false)



    private boolean checked;







    @ManyToOne(fetch = FetchType.LAZY)



    @JoinColumn(name = "document_id")



    private DocumentJpaEntity document;







    @Version



    private Long version;







    public static EvaluatorCheckListJpaEntity of(String content, boolean checked, DocumentJpaEntity document) {



        return EvaluatorCheckListJpaEntity.builder()



                .content(content)



                .checked(checked)



                .document(document)



                .build();



    }







    public void updateChecked(boolean checked) {



        this.checked = checked;



    }



}




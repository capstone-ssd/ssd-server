package or.hyu.ssd.infra.search.elasticsearch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(indexName = "#{@elasticsearchSearchProperties.indexName}", createIndex = false)
@Setting(shards = 1, replicas = 0)
public class ElasticsearchDocument {

    @Id
    private Long documentId;

    @Field(type = FieldType.Long)
    private Long memberId;

    @Field(type = FieldType.Long)
    private Long folderId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @org.springframework.data.elasticsearch.annotations.InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String title;

    @Field(type = FieldType.Text)
    private String keywords;

    @Field(type = FieldType.Keyword)
    private String purpose;

    @Field(type = FieldType.Boolean)
    private boolean bookmark;

    @Field(type = FieldType.Boolean)
    private boolean deleted;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    public static ElasticsearchDocument from(or.hyu.ssd.document.domain.model.Document document) {
        Long memberId = document.getMember() == null ? null : document.getMember().getId();
        Long folderId = document.getFolder() == null ? null : document.getFolder().getId();
        return new ElasticsearchDocument(
                document.getId(),
                memberId,
                folderId,
                document.getTitle(),
                document.getKeywords(),
                document.getPurposeOrDefault().name(),
                document.isBookmark(),
                false,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public or.hyu.ssd.document.domain.model.Document toDomain() {
        or.hyu.ssd.member.domain.model.Member member = memberId == null
                ? null
                : or.hyu.ssd.member.domain.model.Member.builder().id(memberId).build();
        or.hyu.ssd.document.domain.model.Folder folder = folderId == null
                ? null
                : or.hyu.ssd.document.domain.model.Folder.builder().id(folderId).build();
        or.hyu.ssd.document.domain.model.DocumentPurpose resolvedPurpose = purpose == null
                ? or.hyu.ssd.document.domain.model.DocumentPurpose.WRITING
                : or.hyu.ssd.document.domain.model.DocumentPurpose.valueOf(purpose);
        return or.hyu.ssd.document.domain.model.Document.builder()
                .id(documentId)
                .title(title)
                .content("")
                .keywords(keywords)
                .purpose(resolvedPurpose)
                .bookmark(bookmark)
                .member(member)
                .folder(folder)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}

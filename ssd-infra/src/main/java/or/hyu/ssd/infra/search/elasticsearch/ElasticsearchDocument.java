package or.hyu.ssd.infra.search.elasticsearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentPurpose;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.member.domain.model.Member;

import java.time.LocalDateTime;

public record ElasticsearchDocument(
        Long documentId,
        Long memberId,
        Long folderId,
        String title,
        String keywords,
        DocumentPurpose purpose,
        boolean bookmark,
        boolean deleted,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime updatedAt
) {

    public static ElasticsearchDocument from(Document document) {
        Long memberId = document.getMember() == null ? null : document.getMember().getId();
        Long folderId = document.getFolder() == null ? null : document.getFolder().getId();
        return new ElasticsearchDocument(
                document.getId(),
                memberId,
                folderId,
                document.getTitle(),
                document.getKeywords(),
                document.getPurposeOrDefault(),
                document.isBookmark(),
                false,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public Document toDomain() {
        Member member = memberId == null ? null : Member.builder().id(memberId).build();
        Folder folder = folderId == null ? null : Folder.builder().id(folderId).build();
        return Document.builder()
                .id(documentId)
                .title(title)
                .content("")
                .keywords(keywords)
                .purpose(purpose)
                .bookmark(bookmark)
                .member(member)
                .folder(folder)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}

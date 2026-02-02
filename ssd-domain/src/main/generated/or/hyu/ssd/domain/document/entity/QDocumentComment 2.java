package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentComment is a Querydsl query type for DocumentComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentComment extends EntityPathBase<DocumentComment> {

    private static final long serialVersionUID = -786922783L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentComment documentComment = new QDocumentComment("documentComment");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> blockId = createNumber("blockId", Integer.class);

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final or.hyu.ssd.domain.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDocumentComment(String variable) {
        this(DocumentComment.class, forVariable(variable), INITS);
    }

    public QDocumentComment(Path<? extends DocumentComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentComment(PathMetadata metadata, PathInits inits) {
        this(DocumentComment.class, metadata, inits);
    }

    public QDocumentComment(Class<? extends DocumentComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QDocument(forProperty("document"), inits.get("document")) : null;
        this.member = inits.isInitialized("member") ? new or.hyu.ssd.domain.member.entity.QMember(forProperty("member")) : null;
    }

}


package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentParagraph is a Querydsl query type for DocumentParagraph
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentParagraph extends EntityPathBase<DocumentParagraph> {

    private static final long serialVersionUID = -1169210000L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentParagraph documentParagraph = new QDocumentParagraph("documentParagraph");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> blockId = createNumber("blockId", Integer.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> pageNumber = createNumber("pageNumber", Integer.class);

    public final StringPath role = createString("role");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QDocumentParagraph(String variable) {
        this(DocumentParagraph.class, forVariable(variable), INITS);
    }

    public QDocumentParagraph(Path<? extends DocumentParagraph> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentParagraph(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentParagraph(PathMetadata metadata, PathInits inits) {
        this(DocumentParagraph.class, metadata, inits);
    }

    public QDocumentParagraph(Class<? extends DocumentParagraph> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QDocument(forProperty("document"), inits.get("document")) : null;
    }

}


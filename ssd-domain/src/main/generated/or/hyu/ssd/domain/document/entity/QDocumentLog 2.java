package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDocumentLog is a Querydsl query type for DocumentLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDocumentLog extends EntityPathBase<DocumentLog> {

    private static final long serialVersionUID = 1114032326L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDocumentLog documentLog = new QDocumentLog("documentLog");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDocument document;

    public final StringPath editorName = createString("editorName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QDocumentLog(String variable) {
        this(DocumentLog.class, forVariable(variable), INITS);
    }

    public QDocumentLog(Path<? extends DocumentLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDocumentLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDocumentLog(PathMetadata metadata, PathInits inits) {
        this(DocumentLog.class, metadata, inits);
    }

    public QDocumentLog(Class<? extends DocumentLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QDocument(forProperty("document"), inits.get("document")) : null;
    }

}


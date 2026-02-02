package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEvaluatorCheckList is a Querydsl query type for EvaluatorCheckList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEvaluatorCheckList extends EntityPathBase<EvaluatorCheckList> {

    private static final long serialVersionUID = 114556698L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEvaluatorCheckList evaluatorCheckList = new QEvaluatorCheckList("evaluatorCheckList");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    public final BooleanPath checked = createBoolean("checked");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QEvaluatorCheckList(String variable) {
        this(EvaluatorCheckList.class, forVariable(variable), INITS);
    }

    public QEvaluatorCheckList(Path<? extends EvaluatorCheckList> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEvaluatorCheckList(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEvaluatorCheckList(PathMetadata metadata, PathInits inits) {
        this(EvaluatorCheckList.class, metadata, inits);
    }

    public QEvaluatorCheckList(Class<? extends EvaluatorCheckList> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QDocument(forProperty("document"), inits.get("document")) : null;
    }

}


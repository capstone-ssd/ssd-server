package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEvaluatorReview is a Querydsl query type for EvaluatorReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEvaluatorReview extends EntityPathBase<EvaluatorReview> {

    private static final long serialVersionUID = 38788740L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEvaluatorReview evaluatorReview = new QEvaluatorReview("evaluatorReview");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDocument document;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final or.hyu.ssd.domain.member.entity.QMember reviewer;

    public final NumberPath<Integer> scoreDifferentiation = createNumber("scoreDifferentiation", Integer.class);

    public final NumberPath<Integer> scoreFeasibility = createNumber("scoreFeasibility", Integer.class);

    public final NumberPath<Integer> scoreFinancial = createNumber("scoreFinancial", Integer.class);

    public final NumberPath<Double> scoreTotal = createNumber("scoreTotal", Double.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QEvaluatorReview(String variable) {
        this(EvaluatorReview.class, forVariable(variable), INITS);
    }

    public QEvaluatorReview(Path<? extends EvaluatorReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEvaluatorReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEvaluatorReview(PathMetadata metadata, PathInits inits) {
        this(EvaluatorReview.class, metadata, inits);
    }

    public QEvaluatorReview(Class<? extends EvaluatorReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.document = inits.isInitialized("document") ? new QDocument(forProperty("document"), inits.get("document")) : null;
        this.reviewer = inits.isInitialized("reviewer") ? new or.hyu.ssd.domain.member.entity.QMember(forProperty("reviewer")) : null;
    }

}


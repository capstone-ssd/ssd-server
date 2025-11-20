package or.hyu.ssd.domain.document.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCheckList is a Querydsl query type for CheckList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCheckList extends EntityPathBase<CheckList> {

    private static final long serialVersionUID = 345346467L;

    public static final QCheckList checkList = new QCheckList("checkList");

    public final or.hyu.ssd.global.entity.QBaseEntity _super = new or.hyu.ssd.global.entity.QBaseEntity(this);

    public final BooleanPath checked = createBoolean("checked");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCheckList(String variable) {
        super(CheckList.class, forVariable(variable));
    }

    public QCheckList(Path<? extends CheckList> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCheckList(PathMetadata metadata) {
        super(CheckList.class, metadata);
    }

}


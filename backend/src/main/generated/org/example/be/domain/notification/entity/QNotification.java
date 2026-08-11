package org.example.be.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -1157912768L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotification notification = new QNotification("notification");

    public final org.example.be.global.entity.QBase _super = new org.example.be.global.entity.QBase(this);

    public final org.example.be.domain.member.entity.QMember actor;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath message = createString("message");

    public final BooleanPath read = createBoolean("read");

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final org.example.be.domain.member.entity.QMember receiver;

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final StringPath targetName = createString("targetName");

    public final EnumPath<org.example.be.domain.notification.type.NotificationType> type = createEnum("type", org.example.be.domain.notification.type.NotificationType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedTime = _super.updatedTime;

    public QNotification(String variable) {
        this(Notification.class, forVariable(variable), INITS);
    }

    public QNotification(Path<? extends Notification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotification(PathMetadata metadata, PathInits inits) {
        this(Notification.class, metadata, inits);
    }

    public QNotification(Class<? extends Notification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.actor = inits.isInitialized("actor") ? new org.example.be.domain.member.entity.QMember(forProperty("actor")) : null;
        this.receiver = inits.isInitialized("receiver") ? new org.example.be.domain.member.entity.QMember(forProperty("receiver")) : null;
    }

}


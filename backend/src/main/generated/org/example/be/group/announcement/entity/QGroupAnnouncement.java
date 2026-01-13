package org.example.be.group.announcement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupAnnouncement is a Querydsl query type for GroupAnnouncement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupAnnouncement extends EntityPathBase<GroupAnnouncement> {

    private static final long serialVersionUID = 1073891510L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupAnnouncement groupAnnouncement = new QGroupAnnouncement("groupAnnouncement");

    public final StringPath content = createString("content");

    public final org.example.be.group.entitiy.QGroup group;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> timeStamp = createDateTime("timeStamp", java.time.LocalDateTime.class);

    public final StringPath title = createString("title");

    public final StringPath writerName = createString("writerName");

    public QGroupAnnouncement(String variable) {
        this(GroupAnnouncement.class, forVariable(variable), INITS);
    }

    public QGroupAnnouncement(Path<? extends GroupAnnouncement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupAnnouncement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupAnnouncement(PathMetadata metadata, PathInits inits) {
        this(GroupAnnouncement.class, metadata, inits);
    }

    public QGroupAnnouncement(Class<? extends GroupAnnouncement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.group = inits.isInitialized("group") ? new org.example.be.group.entitiy.QGroup(forProperty("group"), inits.get("group")) : null;
    }

}


package org.example.be.group.invitation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupInvitation is a Querydsl query type for GroupInvitation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupInvitation extends EntityPathBase<GroupInvitation> {

    private static final long serialVersionUID = -1535964874L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupInvitation groupInvitation = new QGroupInvitation("groupInvitation");

    public final BooleanPath active = createBoolean("active");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final org.example.be.group.entitiy.QGroup group;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath invitationCode = createString("invitationCode");

    public QGroupInvitation(String variable) {
        this(GroupInvitation.class, forVariable(variable), INITS);
    }

    public QGroupInvitation(Path<? extends GroupInvitation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupInvitation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupInvitation(PathMetadata metadata, PathInits inits) {
        this(GroupInvitation.class, metadata, inits);
    }

    public QGroupInvitation(Class<? extends GroupInvitation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.group = inits.isInitialized("group") ? new org.example.be.group.entitiy.QGroup(forProperty("group"), inits.get("group")) : null;
    }

}


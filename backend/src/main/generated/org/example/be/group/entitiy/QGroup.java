package org.example.be.group.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroup is a Querydsl query type for Group
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroup extends EntityPathBase<Group> {

    private static final long serialVersionUID = 993843051L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroup group = new QGroup("group1");

    public final ListPath<org.example.be.group.announcement.entity.GroupAnnouncement, org.example.be.group.announcement.entity.QGroupAnnouncement> announcements = this.<org.example.be.group.announcement.entity.GroupAnnouncement, org.example.be.group.announcement.entity.QGroupAnnouncement>createList("announcements", org.example.be.group.announcement.entity.GroupAnnouncement.class, org.example.be.group.announcement.entity.QGroupAnnouncement.class, PathInits.DIRECT2);

    public final org.example.be.member.entity.QMember createdBy;

    public final StringPath description = createString("description");

    public final StringPath groupName = createString("groupName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SetPath<org.example.be.member.entity.Member, org.example.be.member.entity.QMember> members = this.<org.example.be.member.entity.Member, org.example.be.member.entity.QMember>createSet("members", org.example.be.member.entity.Member.class, org.example.be.member.entity.QMember.class, PathInits.DIRECT2);

    public QGroup(String variable) {
        this(Group.class, forVariable(variable), INITS);
    }

    public QGroup(Path<? extends Group> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroup(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroup(PathMetadata metadata, PathInits inits) {
        this(Group.class, metadata, inits);
    }

    public QGroup(Class<? extends Group> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.createdBy = inits.isInitialized("createdBy") ? new org.example.be.member.entity.QMember(forProperty("createdBy")) : null;
    }

}


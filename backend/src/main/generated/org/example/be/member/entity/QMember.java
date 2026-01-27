package org.example.be.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 125596686L;

    public static final QMember member = new QMember("member1");

    public final org.example.be.config.QBase _super = new org.example.be.config.QBase(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath name = createString("name");

    public final EnumPath<org.example.be.member.type.OauthProvider> oauthProvider = createEnum("oauthProvider", org.example.be.member.type.OauthProvider.class);

    public final StringPath password = createString("password");

    public final BooleanPath policyAgreed = createBoolean("policyAgreed");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<org.example.be.member.type.MemberRole> role = createEnum("role", org.example.be.member.type.MemberRole.class);

    public final BooleanPath subscribed = createBoolean("subscribed");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedTime = _super.updatedTime;

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}


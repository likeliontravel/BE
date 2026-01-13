package org.example.be.unifieduser.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUnifiedUser is a Querydsl query type for UnifiedUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUnifiedUser extends EntityPathBase<UnifiedUser> {

    private static final long serialVersionUID = 1586881792L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUnifiedUser unifiedUser = new QUnifiedUser("unifiedUser");

    public final StringPath email = createString("email");

    public final org.example.be.generaluser.domain.QGeneralUser generalUser;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final BooleanPath policyAgreed = createBoolean("policyAgreed");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath role = createString("role");

    public final org.example.be.oauth.entity.QSocialUser socialUser;

    public final BooleanPath subscribed = createBoolean("subscribed");

    public final StringPath userIdentifier = createString("userIdentifier");

    public QUnifiedUser(String variable) {
        this(UnifiedUser.class, forVariable(variable), INITS);
    }

    public QUnifiedUser(Path<? extends UnifiedUser> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUnifiedUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUnifiedUser(PathMetadata metadata, PathInits inits) {
        this(UnifiedUser.class, metadata, inits);
    }

    public QUnifiedUser(Class<? extends UnifiedUser> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.generalUser = inits.isInitialized("generalUser") ? new org.example.be.generaluser.domain.QGeneralUser(forProperty("generalUser")) : null;
        this.socialUser = inits.isInitialized("socialUser") ? new org.example.be.oauth.entity.QSocialUser(forProperty("socialUser")) : null;
    }

}


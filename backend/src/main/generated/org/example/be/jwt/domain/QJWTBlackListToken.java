package org.example.be.jwt.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJWTBlackListToken is a Querydsl query type for JWTBlackListToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJWTBlackListToken extends EntityPathBase<JWTBlackListToken> {

    private static final long serialVersionUID = -903815139L;

    public static final QJWTBlackListToken jWTBlackListToken = new QJWTBlackListToken("jWTBlackListToken");

    public final StringPath accessToken = createString("accessToken");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath refreshToken = createString("refreshToken");

    public final StringPath userIdentifier = createString("userIdentifier");

    public QJWTBlackListToken(String variable) {
        super(JWTBlackListToken.class, forVariable(variable));
    }

    public QJWTBlackListToken(Path<? extends JWTBlackListToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJWTBlackListToken(PathMetadata metadata) {
        super(JWTBlackListToken.class, metadata);
    }

}


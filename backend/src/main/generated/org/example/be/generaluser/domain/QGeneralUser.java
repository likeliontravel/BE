package org.example.be.generaluser.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGeneralUser is a Querydsl query type for GeneralUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGeneralUser extends EntityPathBase<GeneralUser> {

    private static final long serialVersionUID = 1915949217L;

    public static final QGeneralUser generalUser = new QGeneralUser("generalUser");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath role = createString("role");

    public final StringPath userIdentifier = createString("userIdentifier");

    public QGeneralUser(String variable) {
        super(GeneralUser.class, forVariable(variable));
    }

    public QGeneralUser(Path<? extends GeneralUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGeneralUser(PathMetadata metadata) {
        super(GeneralUser.class, metadata);
    }

}


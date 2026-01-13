package org.example.be.mail.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMail is a Querydsl query type for Mail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMail extends EntityPathBase<Mail> {

    private static final long serialVersionUID = 1963844295L;

    public static final QMail mail = new QMail("mail");

    public final StringPath authCode = createString("authCode");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public QMail(String variable) {
        super(Mail.class, forVariable(variable));
    }

    public QMail(Path<? extends Mail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMail(PathMetadata metadata) {
        super(Mail.class, metadata);
    }

}


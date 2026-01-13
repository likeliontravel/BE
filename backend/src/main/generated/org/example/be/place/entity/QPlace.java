package org.example.be.place.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlace is a Querydsl query type for Place
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QPlace extends EntityPathBase<Place> {

    private static final long serialVersionUID = 1378698528L;

    public static final QPlace place = new QPlace("place");

    public final StringPath addr1 = createString("addr1");

    public final StringPath addr2 = createString("addr2");

    public final StringPath areaCode = createString("areaCode");

    public final StringPath cat1 = createString("cat1");

    public final StringPath cat2 = createString("cat2");

    public final StringPath cat3 = createString("cat3");

    public final StringPath contentId = createString("contentId");

    public final StringPath createdTime = createString("createdTime");

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Double> mapX = createNumber("mapX", Double.class);

    public final NumberPath<Double> mapY = createNumber("mapY", Double.class);

    public final NumberPath<Integer> mLevel = createNumber("mLevel", Integer.class);

    public final StringPath modifiedTime = createString("modifiedTime");

    public final StringPath siGunGuCode = createString("siGunGuCode");

    public final StringPath tel = createString("tel");

    public final StringPath thumbnailImageUrl = createString("thumbnailImageUrl");

    public final StringPath title = createString("title");

    public QPlace(String variable) {
        super(Place.class, forVariable(variable));
    }

    public QPlace(Path<? extends Place> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlace(PathMetadata metadata) {
        super(Place.class, metadata);
    }

}


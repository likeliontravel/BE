package org.example.be.place.theme;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPlaceCategory is a Querydsl query type for PlaceCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlaceCategory extends EntityPathBase<PlaceCategory> {

    private static final long serialVersionUID = 128106780L;

    public static final QPlaceCategory placeCategory = new QPlaceCategory("placeCategory");

    public final StringPath cat1 = createString("cat1");

    public final StringPath cat2 = createString("cat2");

    public final StringPath cat3 = createString("cat3");

    public final StringPath contentTypeId = createString("contentTypeId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath largeClassification = createString("largeClassification");

    public final StringPath midClassification = createString("midClassification");

    public final StringPath smallClassification = createString("smallClassification");

    public final StringPath theme = createString("theme");

    public QPlaceCategory(String variable) {
        super(PlaceCategory.class, forVariable(variable));
    }

    public QPlaceCategory(Path<? extends PlaceCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlaceCategory(PathMetadata metadata) {
        super(PlaceCategory.class, metadata);
    }

}


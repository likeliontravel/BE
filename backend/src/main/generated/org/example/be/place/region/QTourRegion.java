package org.example.be.place.region;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTourRegion is a Querydsl query type for TourRegion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTourRegion extends EntityPathBase<TourRegion> {

    private static final long serialVersionUID = 1047949122L;

    public static final QTourRegion tourRegion = new QTourRegion("tourRegion");

    public final StringPath areaCode = createString("areaCode");

    public final StringPath areaName = createString("areaName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath region = createString("region");

    public final StringPath siGunGuCode = createString("siGunGuCode");

    public final StringPath siGunGuName = createString("siGunGuName");

    public QTourRegion(String variable) {
        super(TourRegion.class, forVariable(variable));
    }

    public QTourRegion(Path<? extends TourRegion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTourRegion(PathMetadata metadata) {
        super(TourRegion.class, metadata);
    }

}


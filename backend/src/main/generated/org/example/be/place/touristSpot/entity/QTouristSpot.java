package org.example.be.place.touristSpot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTouristSpot is a Querydsl query type for TouristSpot
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTouristSpot extends EntityPathBase<TouristSpot> {

    private static final long serialVersionUID = 1752029063L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTouristSpot touristSpot = new QTouristSpot("touristSpot");

    public final org.example.be.place.entity.QPlace _super = new org.example.be.place.entity.QPlace(this);

    //inherited
    public final StringPath addr1 = _super.addr1;

    //inherited
    public final StringPath addr2 = _super.addr2;

    //inherited
    public final StringPath areaCode = _super.areaCode;

    //inherited
    public final StringPath cat1 = _super.cat1;

    //inherited
    public final StringPath cat2 = _super.cat2;

    //inherited
    public final StringPath cat3 = _super.cat3;

    //inherited
    public final StringPath contentId = _super.contentId;

    //inherited
    public final StringPath createdTime = _super.createdTime;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final StringPath imageUrl = _super.imageUrl;

    //inherited
    public final NumberPath<Double> mapX = _super.mapX;

    //inherited
    public final NumberPath<Double> mapY = _super.mapY;

    //inherited
    public final NumberPath<Integer> mLevel = _super.mLevel;

    //inherited
    public final StringPath modifiedTime = _super.modifiedTime;

    public final org.example.be.place.theme.QPlaceCategory placeCategory;

    //inherited
    public final StringPath siGunGuCode = _super.siGunGuCode;

    //inherited
    public final StringPath tel = _super.tel;

    //inherited
    public final StringPath thumbnailImageUrl = _super.thumbnailImageUrl;

    //inherited
    public final StringPath title = _super.title;

    public final org.example.be.place.region.QTourRegion tourRegion;

    public QTouristSpot(String variable) {
        this(TouristSpot.class, forVariable(variable), INITS);
    }

    public QTouristSpot(Path<? extends TouristSpot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTouristSpot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTouristSpot(PathMetadata metadata, PathInits inits) {
        this(TouristSpot.class, metadata, inits);
    }

    public QTouristSpot(Class<? extends TouristSpot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.placeCategory = inits.isInitialized("placeCategory") ? new org.example.be.place.theme.QPlaceCategory(forProperty("placeCategory")) : null;
        this.tourRegion = inits.isInitialized("tourRegion") ? new org.example.be.place.region.QTourRegion(forProperty("tourRegion")) : null;
    }

}


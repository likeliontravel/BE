package org.example.be.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchedulePlace is a Querydsl query type for SchedulePlace
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSchedulePlace extends EntityPathBase<SchedulePlace> {

    private static final long serialVersionUID = 307557407L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSchedulePlace schedulePlace = new QSchedulePlace("schedulePlace");

    public final org.example.be.config.QBase _super = new org.example.be.config.QBase(this);

    public final StringPath contentId = createString("contentId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    public final NumberPath<Integer> dayOrder = createNumber("dayOrder", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Integer> orderInDay = createNumber("orderInDay", Integer.class);

    public final EnumPath<org.example.be.place.entity.PlaceType> placeType = createEnum("placeType", org.example.be.place.entity.PlaceType.class);

    public final QSchedule schedule;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedTime = _super.updatedTime;

    public final DateTimePath<java.time.LocalDateTime> visitedEnd = createDateTime("visitedEnd", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> visitStart = createDateTime("visitStart", java.time.LocalDateTime.class);

    public QSchedulePlace(String variable) {
        this(SchedulePlace.class, forVariable(variable), INITS);
    }

    public QSchedulePlace(Path<? extends SchedulePlace> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSchedulePlace(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSchedulePlace(PathMetadata metadata, PathInits inits) {
        this(SchedulePlace.class, metadata, inits);
    }

    public QSchedulePlace(Class<? extends SchedulePlace> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.schedule = inits.isInitialized("schedule") ? new QSchedule(forProperty("schedule"), inits.get("schedule")) : null;
    }

}


package org.example.be.schedule.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSchedule is a Querydsl query type for Schedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSchedule extends EntityPathBase<Schedule> {

    private static final long serialVersionUID = 224973256L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSchedule schedule = new QSchedule("schedule");

    public final DateTimePath<java.time.LocalDateTime> endSchedule = createDateTime("endSchedule", java.time.LocalDateTime.class);

    public final org.example.be.group.entitiy.QGroup group;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<SchedulePlace, QSchedulePlace> schedulePlaces = this.<SchedulePlace, QSchedulePlace>createList("schedulePlaces", SchedulePlace.class, QSchedulePlace.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> startSchedule = createDateTime("startSchedule", java.time.LocalDateTime.class);

    public QSchedule(String variable) {
        this(Schedule.class, forVariable(variable), INITS);
    }

    public QSchedule(Path<? extends Schedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSchedule(PathMetadata metadata, PathInits inits) {
        this(Schedule.class, metadata, inits);
    }

    public QSchedule(Class<? extends Schedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.group = inits.isInitialized("group") ? new org.example.be.group.entitiy.QGroup(forProperty("group"), inits.get("group")) : null;
    }

}


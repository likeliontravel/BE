package org.example.be.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QScheduleReminderLog is a Querydsl query type for ScheduleReminderLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScheduleReminderLog extends EntityPathBase<ScheduleReminderLog> {

    private static final long serialVersionUID = 1751623270L;

    public static final QScheduleReminderLog scheduleReminderLog = new QScheduleReminderLog("scheduleReminderLog");

    public final org.example.be.global.entity.QBase _super = new org.example.be.global.entity.QBase(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdTime = _super.createdTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DatePath<java.time.LocalDate> reminderDate = createDate("reminderDate", java.time.LocalDate.class);

    public final EnumPath<org.example.be.domain.notification.type.ReminderType> reminderType = createEnum("reminderType", org.example.be.domain.notification.type.ReminderType.class);

    public final NumberPath<Long> scheduleId = createNumber("scheduleId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedTime = _super.updatedTime;

    public QScheduleReminderLog(String variable) {
        super(ScheduleReminderLog.class, forVariable(variable));
    }

    public QScheduleReminderLog(Path<? extends ScheduleReminderLog> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScheduleReminderLog(PathMetadata metadata) {
        super(ScheduleReminderLog.class, metadata);
    }

}


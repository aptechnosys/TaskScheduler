package com.example.TaskScheduler.domain.model

import com.example.TaskScheduler.data.local.AlarmEntity

data class Alarm(
    val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isAlarmSet: Boolean,
    val isNotificationSet: Boolean,
    val createdDate: Long
)

// Converts domain model to Room entity
fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    title = title,
    description = description,
    timestamp = timestamp,
    isAlarmSet = isAlarmSet,
    isNotificationSet = isNotificationSet,
    createdDate = createdDate
)

// Converts Room entity to domain model
fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    title = title,
    description = description,
    timestamp = timestamp,
    isAlarmSet = isAlarmSet,
    isNotificationSet = isNotificationSet,
    createdDate = createdDate
)

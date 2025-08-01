package com.example.TaskScheduler.data.repository

import com.example.TaskScheduler.data.local.AlarmDao
import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.domain.model.toDomain
import com.example.TaskScheduler.domain.model.toEntity
import com.example.TaskScheduler.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao
) : AlarmRepository {
    override suspend fun addAlarm(alarm: Alarm) = dao.insert(alarm.toEntity())
    override fun getAlarms() = dao.getAll().map { it.map { it.toDomain() } }
    override suspend fun deleteAlarm(alarm: Alarm) {
        dao.deleteAlarm(alarm.toEntity())
    }
}

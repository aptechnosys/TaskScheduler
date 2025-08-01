package com.example.TaskScheduler.domain.repository

import com.example.TaskScheduler.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun addAlarm(alarm: Alarm)
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun deleteAlarm(alarm: Alarm)
}
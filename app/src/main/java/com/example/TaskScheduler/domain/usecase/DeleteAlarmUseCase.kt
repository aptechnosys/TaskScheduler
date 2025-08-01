package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.domain.repository.AlarmRepository
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) {
        repository.deleteAlarm(alarm)
    }
}
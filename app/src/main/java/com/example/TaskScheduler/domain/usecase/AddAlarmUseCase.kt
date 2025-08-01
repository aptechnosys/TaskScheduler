package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.domain.repository.AlarmRepository
import javax.inject.Inject

class AddAlarmUseCase @Inject constructor(
    private val repo: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) = repo.addAlarm(alarm)
}
package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.repository.AlarmRepository
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val repo: AlarmRepository
) {
    operator fun invoke() = repo.getAlarms()
}
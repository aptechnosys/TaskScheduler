package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    operator fun invoke() = repo.getPayment()
}
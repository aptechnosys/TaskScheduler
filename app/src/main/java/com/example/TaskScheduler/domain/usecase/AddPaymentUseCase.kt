package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.model.Payment
import com.example.TaskScheduler.domain.repository.PaymentRepository
import javax.inject.Inject

class AddPaymentUseCase @Inject constructor(
    private val repo: PaymentRepository
) {
    suspend operator fun invoke(payment: Payment) = repo.addPayment(payment)
}

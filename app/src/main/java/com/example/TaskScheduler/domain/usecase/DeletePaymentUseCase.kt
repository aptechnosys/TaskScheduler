package com.example.TaskScheduler.domain.usecase

import com.example.TaskScheduler.domain.model.Payment
import com.example.TaskScheduler.domain.repository.PaymentRepository
import javax.inject.Inject

class DeletePaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(payment: Payment) {
        repository.deletePayment(payment)
    }
}
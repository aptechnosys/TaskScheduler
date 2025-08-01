package com.example.TaskScheduler.domain.repository

import com.example.TaskScheduler.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun addPayment(payment: Payment)
    fun getPayment(): Flow<List<Payment>>
    suspend fun deletePayment(payment: Payment)
}
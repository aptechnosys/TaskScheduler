package com.example.TaskScheduler.data.repository

import com.example.TaskScheduler.data.local.PaymentDao
import com.example.TaskScheduler.domain.model.Payment
import com.example.TaskScheduler.domain.model.toDomain
import com.example.TaskScheduler.domain.model.toEntity
import com.example.TaskScheduler.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class PaymentRepositoryImpl @Inject constructor(
    private val dao: PaymentDao
) : PaymentRepository {

    override suspend fun addPayment(payment: Payment) {
        dao.insert(payment.toEntity())
    }
    override fun getPayment(): Flow<List<Payment>> {
        return dao.getAll().map { it.map { it.toDomain() } }
    }
    override suspend fun deletePayment(payment: Payment) {
        dao.deletePayment(payment.toEntity())
    }

}

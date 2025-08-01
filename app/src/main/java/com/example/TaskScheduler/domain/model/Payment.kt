package com.example.TaskScheduler.domain.model

import com.example.TaskScheduler.data.local.PaymentEntity
import kotlin.Long

data class Payment(
    val timestamp: Long,
    val isSuccessful: Boolean
)

// Converts domain model to Room entity
fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    timestamp = timestamp,
    isSuccessful = isSuccessful
)

// Converts Room entity to domain model
fun PaymentEntity.toDomain(): Payment = Payment(
    timestamp = timestamp,
    isSuccessful = isSuccessful
)

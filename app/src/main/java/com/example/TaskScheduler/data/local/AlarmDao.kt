package com.example.TaskScheduler.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isAlarmSet: Boolean,
    val isNotificationSet: Boolean,
    val createdDate: Long
)

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms ORDER BY timestamp ASC")
    fun getAll(): Flow<List<AlarmEntity>>

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val isSuccessful: Boolean
)

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Query("SELECT * FROM payments ORDER BY timestamp ASC")
    fun getAll(): Flow<List<PaymentEntity>>

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

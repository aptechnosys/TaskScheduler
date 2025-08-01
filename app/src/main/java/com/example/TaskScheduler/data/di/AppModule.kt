package com.example.TaskScheduler.data.di

import android.app.Application
import androidx.room.Room
import com.example.TaskScheduler.data.local.AlarmDatabase
import com.example.TaskScheduler.data.repository.AlarmRepositoryImpl
import com.example.TaskScheduler.domain.repository.AlarmRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.TaskScheduler.data.local.PaymentDatabase
import com.example.TaskScheduler.domain.repository.PaymentRepository
import com.example.TaskScheduler.data.repository.PaymentRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(app: Application): AlarmDatabase {
        return Room.databaseBuilder(
            app,
            AlarmDatabase::class.java,
            "alarm_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(db: AlarmDatabase): AlarmRepository {
        return AlarmRepositoryImpl(db.alarmDao())
    }

    @Provides
    @Singleton
    fun providePaymentDatabase(app: Application): PaymentDatabase {
        return Room.databaseBuilder(
            app,
            PaymentDatabase::class.java,
            "payment_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePaymentRepository(db: PaymentDatabase): PaymentRepository {
        return PaymentRepositoryImpl(db.paymentDao())
    }
}
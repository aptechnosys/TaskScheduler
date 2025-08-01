package com.example.TaskScheduler.presentation

import androidx.lifecycle.ViewModel
import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.domain.usecase.AddAlarmUseCase
import com.example.TaskScheduler.domain.usecase.GetAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.TaskScheduler.domain.model.Payment
import com.example.TaskScheduler.domain.usecase.AddPaymentUseCase
import com.example.TaskScheduler.domain.usecase.DeleteAlarmUseCase
import com.example.TaskScheduler.domain.usecase.DeletePaymentUseCase
import com.example.TaskScheduler.domain.usecase.GetPaymentUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val addAlarmUseCase: AddAlarmUseCase,
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val addPaymentUseCase: AddPaymentUseCase,
    private val getPaymentUseCase: GetPaymentUseCase,
    private val deletePaymentUseCase: DeletePaymentUseCase,

    ) : ViewModel() {

    private val _alarmList = MutableStateFlow<List<Alarm>>(emptyList())
    val alarmList: StateFlow<List<Alarm>> = _alarmList.asStateFlow()

    private val _paymentList = MutableStateFlow<List<Payment>>(emptyList())
    val paymentList: StateFlow<List<Payment>> = _paymentList.asStateFlow()

    // Separate flows
    val alarmOnlyList: StateFlow<List<Alarm>> = _alarmList
        .map { list -> list.filter { it.isAlarmSet }
            .sortedByDescending { it.timestamp }}
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notificationOnlyList: StateFlow<List<Alarm>> = _alarmList
        .map { list -> list.filter { it.isNotificationSet }
            .sortedByDescending { it.timestamp }}
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            getAlarmsUseCase().collect {
                _alarmList.value = it
            }
        }

        viewModelScope.launch {
            getPaymentUseCase().collect {
                _paymentList.value = it
            }
        }
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            addAlarmUseCase(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)  // Ensure you have this use case and repository method
        }
    }

    fun addPayment(payment: Payment) {
        viewModelScope.launch {
            addPaymentUseCase(payment)
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            deletePaymentUseCase(payment)
        }
    }
}
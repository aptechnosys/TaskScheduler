package com.example.TaskScheduler.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.stopService(Intent(context, AlarmForegroundService::class.java))
    }
}
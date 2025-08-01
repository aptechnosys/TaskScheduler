package com.example.TaskScheduler.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.TaskScheduler.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Triggered at: ${System.currentTimeMillis()}")

        val isNotification = intent.getBooleanExtra("SHOW_NOTIFICATION", false)

        if (isNotification) {
            showStandardNotification(context, intent)
        } else {
            val title = intent.getStringExtra("title") ?: "Alarm"
            val desc = intent.getStringExtra("desc") ?: "Wake up!"
//            val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
//                putExtra("title", title)
//                putExtra("desc", desc)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }
//
//            try {
//                context.startActivity(fullScreenIntent)
//            } catch (e: Exception) {
//                Log.e("AlarmReceiver", "Unable to launch AlarmRingActivity", e)
//            }
//
//            Toast.makeText(context, "⏰ Alarm Triggered!", Toast.LENGTH_SHORT).show()
          //  showFullScreenNotification(context, intent)

            val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                putExtra("title", title)
                putExtra("desc", desc)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun showFullScreenNotification(context: Context, intent: Intent) {
        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmTitle = intent.getStringExtra("title") ?: "Alarm"
        val alarmDesc = intent.getStringExtra("desc") ?: "Alarm triggered!"

        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create channel (but don't rely on its sound anymore)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for full-screen alarm alerts"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null) // ❌ Disable channel-based sound
            }
            notificationManager.createNotificationChannel(channel)
        }


        // Remote View
        val customView = RemoteViews(context.packageName, R.layout.notification_alarm)
        customView.setTextViewText(R.id.tvAlarmTitle, alarmTitle)
        customView.setTextViewText(R.id.tvAlarmDesc, alarmDesc)


        // PendingIntent for Stop Button
        val stopIntent = Intent(context, AlarmRingActivity::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        customView.setOnClickPendingIntent(R.id.btnStopAlarm, stopPendingIntent)

//        // Prepare full screen intent
//        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("title", alarmTitle)
//            putExtra("desc", alarmDesc)
//            putExtra("alarm_uri", alarmSoundUri.toString())
//        }

//        val fullScreenPendingIntent = PendingIntent.getActivity(
//            context,
//            101,
//            fullScreenIntent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )

        val builder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_alarm)
            .setCustomContentView(customView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, builder.build())

        // ✅ Play sound manually using MediaPlayer
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmSoundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            // Optional: stop after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
            }, 30_000)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to play alarm sound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStandardNotification(context: Context, intent: Intent) {
        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8+ requires a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Standard notifications for reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val clickIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
            .setContentTitle(intent.getStringExtra("title") ?: "Reminder")
            .setContentText(intent.getStringExtra("desc") ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.presentation.AlarmReceiver

object AlarmScheduler {

    fun scheduleAlarm(context: Context, timeInMillis: Long, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ Android 12+ exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Please enable 'Schedule Exact Alarms' permission in settings",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        Log.d("xjksdfsd", alarm.toString())

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", alarm.title)
            putExtra("desc", alarm.description)
            if (alarm.isNotificationSet) {
                putExtra("SHOW_NOTIFICATION", true)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,  // Make sure ID is unique
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }
}
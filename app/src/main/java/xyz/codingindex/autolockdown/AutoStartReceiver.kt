package xyz.codingindex.autolockdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).also { am ->
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 100,
                100,
                PendingIntent.getForegroundService(
                    context, 0, Intent(context, ScreenStatusService::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }
}
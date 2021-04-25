package xyz.codingindex.autolockdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStartReceiver : BroadcastReceiver() {

    fun isEnabled(context: Context): Boolean {
        return context
            .getSharedPreferences(context.getString(R.string.ald_sp_key), Context.MODE_PRIVATE)
            .getBoolean("enabled", false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isEnabled(context)) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).also { am ->
                am.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 100,
                    PendingIntent.getForegroundService(
                        context, 0, Intent(context, ScreenStatusService::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
        }
    }
}
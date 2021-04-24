package xyz.codingindex.autolockdown

import android.app.AlarmManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import xyz.codingindex.autolockdown.common.Lockdown

class ScreenStatusReceiver : BroadcastReceiver(), AlarmManager.OnAlarmListener {
    var mContext: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        mContext = context

        if (intent.action.equals(Intent.ACTION_SCREEN_OFF)) {
            var duration = 5
            mContext?.getSharedPreferences(mContext?.getString(R.string.ald_sp_key),
                Context.MODE_PRIVATE)?.getInt("duration", 5)?.let {
                duration = it
            }
            alarmManager?.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + duration * 60000,
                "automatic lockdown",
                this,
                null)
        } else if (intent.action.equals(Intent.ACTION_SCREEN_ON)) {
            alarmManager?.cancel(this)
        } else if (intent.action.equals(Intent.ACTION_USER_PRESENT)) {
            if (mContext != null) {
                Lockdown.release(mContext!!)
            }
            alarmManager?.cancel(this)
        }
    }

    override fun onAlarm() {
        if (mContext != null) {
            Lockdown.lockdown(mContext!!)
        }
    }
}
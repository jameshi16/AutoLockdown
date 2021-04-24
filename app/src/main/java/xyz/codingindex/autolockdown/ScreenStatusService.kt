package xyz.codingindex.autolockdown

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder

class ScreenStatusService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    var mScreenStatusReceiver: ScreenStatusReceiver? = null
    var mNotificationChannel: NotificationChannel? = null
    var mSharedPreferences: SharedPreferences? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        mSharedPreferences =
            getSharedPreferences(getString(R.string.ald_sp_key), Context.MODE_PRIVATE).also {
                it.registerOnSharedPreferenceChangeListener(this)
            }

        mScreenStatusReceiver = ScreenStatusReceiver().also {
            registerReceiver(it, IntentFilter().apply {
                this.addAction(Intent.ACTION_SCREEN_OFF)
                this.addAction(Intent.ACTION_SCREEN_ON)
                this.addAction(Intent.ACTION_USER_PRESENT)
            })
        }

        val notificationManager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        mNotificationChannel = NotificationChannel(
            getString(R.string.ald_noti),
            getString(R.string.ald_noti_name),
            NotificationManager.IMPORTANCE_HIGH
        ).also {
            notificationManager.createNotificationChannel(it)
            Notification.Builder(applicationContext, it.id).apply {
                this.setContentTitle(getString(R.string.ald_noti_name))
                this.setContentText(getString(R.string.ald_noti_desc))
                this.setSmallIcon(R.drawable.outline_lock_24)
                this.setChannelId(it.id)
                this.setOngoing(true)
            }.build().also {
                startForeground(1, it)
            }
        }
    }

    override fun onDestroy() {
        try {
            if (mScreenStatusReceiver != null) {
                unregisterReceiver(mScreenStatusReceiver)
            }

            if (mNotificationChannel != null) {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .deleteNotificationChannel(mNotificationChannel!!.id)
            }

            if (mSharedPreferences != null) {
                mSharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
            }
        } catch (e: IllegalArgumentException) {
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null && key.equals("enabled")) {
            if (!sharedPreferences.getBoolean(key, false)) {
                stopSelf()
            }
        }
    }
}
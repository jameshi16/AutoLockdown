package xyz.codingindex.autolockdown

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.edit

class AutoLockdownTileService : TileService() {
    var mSharedPreferences: SharedPreferences? = null

    fun getEnabled(): Boolean {
        return if (mSharedPreferences != null)
            mSharedPreferences!!.getBoolean("enabled", false)
        else false
    }

    fun setEnabled(value: Boolean) {
        mSharedPreferences?.edit {
            this.putBoolean("enabled", value)
            this.apply()
        }

        this.qsTile.state = if (getEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        this.qsTile.updateTile()
    }

    override fun onBind(intent: Intent?): IBinder {
        mSharedPreferences =
            getSharedPreferences(getString(R.string.ald_sp_key), Context.MODE_PRIVATE)
        return super.onBind(intent)
    }

    override fun onClick() {
        super.onClick()

        if (this.qsTile.state == Tile.STATE_ACTIVE) {
            this.setEnabled(false)
        } else {
            this.setEnabled(true)
            startForegroundService(Intent(applicationContext, ScreenStatusService::class.java))
        }
    }

    override fun onStartListening() {
        val enabled = getEnabled()
        this.qsTile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        this.qsTile.updateTile()

        if (enabled) {
            startForegroundService(Intent(applicationContext, ScreenStatusService::class.java))
        }

        super.onStartListening()
    }

    override fun onStopListening() {
        this.qsTile.state = if (getEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        this.qsTile.updateTile()

        super.onStopListening()
    }
}
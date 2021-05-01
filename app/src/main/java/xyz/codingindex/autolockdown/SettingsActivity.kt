package xyz.codingindex.autolockdown

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.h6ah4i.android.preference.NumberPickerPreferenceCompat
import com.h6ah4i.android.preference.NumberPickerPreferenceDialogFragmentCompat
import xyz.codingindex.autolockdown.common.Lockdown

class SettingsActivity : AppCompatActivity() {
    private val DEVICE_ADMIN_RQ = 1
    private val RELAUNCH_APP_RQ = 2

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DEVICE_ADMIN_RQ && resultCode == Activity.RESULT_OK) {
            (applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)
                ?.set(
                    AlarmManager.RTC, System.currentTimeMillis() + 100,
                    PendingIntent.getActivity(
                        applicationContext,
                        RELAUNCH_APP_RQ,
                        Intent(applicationContext, SettingsActivity::class.java),
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                )
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?)?.also { mDPM ->
            ComponentName(this, AdminReceiver::class.java).also { mAdminName ->
                if (!mDPM.isAdminActive(mAdminName)) {
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        this.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName)
                        this.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Disabling insecure methods of authentication requires admin access. Click on 'Activate' to give AutoLockdown admin access."
                        )
                        startActivityForResult(this, DEVICE_ADMIN_RQ)

                        getSharedPreferences(
                            getString(R.string.ald_sp_key),
                            Context.MODE_PRIVATE
                        ).let {
                            it.edit {
                                this.putBoolean("enabled", false)
                                this.apply()
                            }
                        }
                        return
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
        var enableAutoLockDown: SwitchPreference? = null
        var durationAutoLockDown: NumberPickerPreferenceDialogFragmentCompat? = null
        var mSharedPreferences: SharedPreferences? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            mSharedPreferences =
                context?.getSharedPreferences(getString(R.string.ald_sp_key), Context.MODE_PRIVATE)
            mSharedPreferences?.registerOnSharedPreferenceChangeListener(this)
            val lockdownEnabled = mSharedPreferences?.getBoolean("enabled", false)

            enableAutoLockDown = (findPreference("enabled") as SwitchPreference?)?.also {
                it.onPreferenceChangeListener = this
                it.setDefaultValue(lockdownEnabled)
            }

            (findPreference("duration") as Preference?)?.also {
                it.onPreferenceChangeListener = this
            }

            if (lockdownEnabled == true) {
                context?.apply {
                    this.startForegroundService(Intent(this, ScreenStatusService::class.java))
                }
            }
        }

        override fun onResume() {
            super.onResume()

            mSharedPreferences?.getBoolean("enabled", false)?.let {
                enableAutoLockDown?.isChecked = it
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            if (parentFragmentManager.findFragmentByTag("androidx.preference.PreferenceFragment.DIALOG") != null) {
                return
            }

            if (preference is NumberPickerPreferenceCompat) {
                preference.value = if (mSharedPreferences != null) mSharedPreferences!!.getInt(
                    "duration",
                    5
                ) else 5
                durationAutoLockDown =
                    NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.key)
                        .also {
                            it.setTargetFragment(this, 0)
                            it.show(
                                parentFragmentManager,
                                "androidx.preference.PreferenceFragment.DIALOG"
                            )
                        }
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            if (preference is SwitchPreference && newValue is Boolean) {
                with(mSharedPreferences?.edit()) {
                    this?.putBoolean("enabled", newValue)
                    this?.apply()
                }
            }

            if (preference is NumberPickerPreferenceCompat && newValue is Int) {
                with(mSharedPreferences?.edit()) {
                    this?.putInt("duration", newValue)
                    this?.apply()
                }
            }
            return true
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (sharedPreferences != null && key.equals("enabled")) {
                if (sharedPreferences.getBoolean(key, false)) {
                    context?.startForegroundService(
                        Intent(
                            this.context,
                            ScreenStatusService::class.java
                        )
                    )
                } else {
                    context?.let {
                        Lockdown.release(it)
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()

            context?.getSharedPreferences(getString(R.string.ald_sp_key), Context.MODE_PRIVATE)
                ?.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}
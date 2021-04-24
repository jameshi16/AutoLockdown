package xyz.codingindex.autolockdown.common

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import xyz.codingindex.autolockdown.AdminReceiver

class Lockdown {
    companion object {
        fun lockdown(context: Context) {
            (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?)?.also { dpm ->
                ComponentName(context, AdminReceiver::class.java).also { cn ->
                    if (dpm.isAdminActive(cn)) {
                        dpm.setKeyguardDisabledFeatures(cn,
                            DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS)
                    }
                }
            }
        }

        fun release(context: Context) {
            (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?)?.also { dpm ->
                ComponentName(context, AdminReceiver::class.java).also { cn ->
                    if (dpm.isAdminActive(cn)) {
                        dpm.setKeyguardDisabledFeatures(cn, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE)
                    }
                }
            }
        }
    }
}
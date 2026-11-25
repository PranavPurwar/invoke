package org.invokevirtual.launcher.system

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

enum class DoubleTapAction {
    NONE,
    LOCK,
    OPEN_SETTINGS,
}

internal fun doubleTapAction(
    enabled: Boolean,
    supported: Boolean,
    excluded: Boolean,
    isEditMode: Boolean,
    serviceConnected: Boolean,
): DoubleTapAction = when {
    !enabled || !supported || excluded || isEditMode -> DoubleTapAction.NONE
    serviceConnected -> DoubleTapAction.LOCK
    else -> DoubleTapAction.OPEN_SETTINGS
}

/** Checks and requests the accessibility access that double-tap to sleep depends on. */
object ScreenLock {
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    fun isServiceEnabled(context: Context): Boolean {
        val manager = context.getSystemService(AccessibilityManager::class.java) ?: return false
        val component = ComponentName(context, ScreenLockService::class.java)
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.let { info -> ComponentName(info.packageName, info.name) } == component }
    }

    fun openSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}

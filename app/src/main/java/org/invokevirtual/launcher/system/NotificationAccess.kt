package org.invokevirtual.launcher.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/** Checks and requests the notification access that badges depend on. */
object NotificationAccess {
    fun isGranted(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    /** Opens Invoke's own access page where the system has one, else the full listener list. */
    fun openSettings(context: Context) {
        val list = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        val detail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
                Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                ComponentName(context, NotificationBadgeListener::class.java).flattenToString(),
            )
        } else {
            null
        }
        listOfNotNull(detail, list).firstOrNull { intent ->
            runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
        }
    }
}

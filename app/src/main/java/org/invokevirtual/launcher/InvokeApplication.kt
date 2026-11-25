package org.invokevirtual.launcher

import android.app.Application
import org.invokevirtual.launcher.data.HomeCanvasStore
import org.invokevirtual.launcher.data.NotificationBadgeStore
import org.invokevirtual.launcher.data.QuickContactsStore
import org.invokevirtual.launcher.data.SettingsStore
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.WidgetHostController

/**
 * Simple manual dependency container—no DI framework or annotation-processing tax.
 */
class InvokeApplication : Application() {
    val settingsStore: SettingsStore by lazy { SettingsStore(applicationContext) }
    val homeCanvasStore: HomeCanvasStore by lazy { HomeCanvasStore(applicationContext) }
    val quickContactsStore: QuickContactsStore by lazy { QuickContactsStore(applicationContext) }
    val appCatalog: AppCatalog by lazy { AppCatalog(applicationContext) }
    val widgetHost: WidgetHostController by lazy { WidgetHostController(applicationContext) }
    val notificationBadgeStore: NotificationBadgeStore by lazy { NotificationBadgeStore() }
}

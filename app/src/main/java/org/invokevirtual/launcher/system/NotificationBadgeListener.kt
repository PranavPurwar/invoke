package org.invokevirtual.launcher.system

import android.app.Notification
import android.os.UserManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.invokevirtual.launcher.InvokeApplication
import org.invokevirtual.launcher.data.NotificationBadgeStore

/**
 * Recounts badges from the full list of active notifications whenever it changes, so a missed
 * callback never leaves a stale count behind.
 */
class NotificationBadgeListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshRequests = Channel<Unit>(Channel.CONFLATED)
    @Volatile
    private var connected = false

    private val store: NotificationBadgeStore
        get() = (applicationContext as InvokeApplication).notificationBadgeStore

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            for (request in refreshRequests) {
                if (connected) store.publish(countBadges(snapshot()))
            }
        }
    }

    override fun onListenerConnected() {
        connected = true
        refreshRequests.trySend(Unit)
    }

    override fun onListenerDisconnected() {
        connected = false
        store.clear()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        refreshRequests.trySend(Unit)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) {
        refreshRequests.trySend(Unit)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) {
        refreshRequests.trySend(Unit)
    }

    override fun onDestroy() {
        connected = false
        scope.cancel()
        store.clear()
        super.onDestroy()
    }

    private fun snapshot(): List<BadgeCandidate> {
        // Throws SecurityException once the user revokes access.
        val active = runCatching { activeNotifications }.getOrNull() ?: return emptyList()
        val rankings = runCatching { currentRanking }.getOrNull() ?: return emptyList()
        val userManager = getSystemService(UserManager::class.java)
        val ranking = Ranking()
        return active.map { sbn ->
            val ranked = rankings.getRanking(sbn.key, ranking)
            val extras = sbn.notification.extras
            BadgeCandidate(
                packageName = sbn.packageName,
                userSerial = userManager?.getSerialNumberForUser(sbn.user) ?: 0L,
                number = sbn.notification.number,
                isGroupSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
                isOngoing = sbn.isOngoing,
                channelId = if (ranked) ranking.channel?.id else sbn.notification.channelId,
                canShowBadge = !ranked || ranking.canShowBadge(),
                hasContent = !extras.getCharSequence(Notification.EXTRA_TITLE).isNullOrBlank() ||
                    !extras.getCharSequence(Notification.EXTRA_TEXT).isNullOrBlank(),
            )
        }
    }
}

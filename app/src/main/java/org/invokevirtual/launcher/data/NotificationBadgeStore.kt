package org.invokevirtual.launcher.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.invokevirtual.launcher.system.BadgeKey

/** Process-wide badge counts, written by the notification listener and read by the UI. */
class NotificationBadgeStore {
    private val _counts = MutableStateFlow<Map<BadgeKey, Int>>(emptyMap())
    val counts: StateFlow<Map<BadgeKey, Int>> = _counts.asStateFlow()

    fun publish(counts: Map<BadgeKey, Int>) {
        _counts.value = counts
    }

    fun clear() {
        _counts.value = emptyMap()
    }
}

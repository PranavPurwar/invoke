package org.invokevirtual.launcher.system

import androidx.compose.runtime.Immutable

/** Badges are per package and profile, so every launcher activity of an app shares one. */
@Immutable
data class BadgeKey(
    val packageName: String,
    val userSerial: Long,
)

/** The parts of an active notification that decide whether and how much it adds to a badge. */
data class BadgeCandidate(
    val packageName: String,
    val userSerial: Long,
    val number: Int,
    val isGroupSummary: Boolean,
    val isOngoing: Boolean,
    val channelId: String?,
    val canShowBadge: Boolean,
    val hasContent: Boolean,
)

val AppKey.badgeKey: BadgeKey
    get() = BadgeKey(packageName, userSerial)

private const val DEFAULT_CHANNEL_ID = "miscellaneous"
private const val MAX_BADGE_COUNT = 99

// Follows AOSP Launcher3's NotificationListener.notificationIsValidForUI.
internal fun countBadges(candidates: List<BadgeCandidate>): Map<BadgeKey, Int> =
    candidates
        .filter { it.canShowBadge }
        .filterNot { it.isOngoing && it.channelId == DEFAULT_CHANNEL_ID }
        .filterNot { it.isGroupSummary }
        .filter { it.hasContent }
        .groupingBy { BadgeKey(it.packageName, it.userSerial) }
        .fold(0) { total, candidate -> total + maxOf(1, candidate.number) }

internal fun Map<BadgeKey, Int>.totalFor(apps: List<AppKey>): Int =
    apps.map { it.badgeKey }.distinct().sumOf { this[it] ?: 0 }

internal fun badgeDescription(count: Int): String =
    if (count == 1) "1 notification" else "$count notifications"

internal fun badgeLabel(count: Int): String =
    if (count > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else count.toString()

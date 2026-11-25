package org.invokevirtual.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.BadgeStyle
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.BadgeKey
import org.invokevirtual.launcher.system.badgeDescription
import org.invokevirtual.launcher.system.badgeLabel
import org.invokevirtual.launcher.system.totalFor

@Immutable
data class NotificationBadges(
    val style: BadgeStyle,
    val counts: Map<BadgeKey, Int>,
) {
    companion object {
        val None = NotificationBadges(BadgeStyle.OFF, emptyMap())
    }
}

/** Dynamic so a count change recomposes only the badges, not every icon. */
val LocalNotificationBadges = compositionLocalOf { NotificationBadges.None }

/** Draws [content] with the notification badge of [apps] on its top end corner. */
@Composable
fun NotificationBadgeBox(
    apps: List<AppKey>,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
        NotificationBadge(
            apps = apps,
            iconSize = iconSize,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
fun NotificationBadge(
    apps: List<AppKey>,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val badges = LocalNotificationBadges.current
    if (badges.style == BadgeStyle.OFF) return
    val total = badges.counts.totalFor(apps)
    if (total == 0) return

    val ring = MaterialTheme.colorScheme.surface
    val fill = MaterialTheme.colorScheme.error
    val described = modifier.clearAndSetSemantics { contentDescription = badgeDescription(total) }

    if (badges.style == BadgeStyle.DOT) {
        val dot = (iconSize * 0.2f).coerceIn(8.dp, 14.dp)
        Box(
            modifier = described
                .size(dot)
                .background(ring, CircleShape)
                .padding(1.5.dp)
                .background(fill, CircleShape),
        )
    } else {
        val height = (iconSize * 0.3f).coerceIn(14.dp, 20.dp)
        val fontSize = with(LocalDensity.current) { (height * 0.6f).toSp() }
        Box(
            modifier = described
                .height(height)
                .widthIn(min = height)
                .background(ring, CircleShape)
                .padding(1.5.dp)
                .background(fill, CircleShape)
                .padding(horizontal = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = badgeLabel(total),
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }
    }
}

package org.invokevirtual.launcher.ui.home

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/** Parts of the home screen that do not count as empty space for double-tap to sleep. */
internal class DoubleTapExclusions {
    private val regions = HashMap<Any, Rect>()

    fun update(key: Any, bounds: Rect) {
        regions[key] = bounds
    }

    fun remove(key: Any) {
        regions.remove(key)
    }

    fun contains(pointInRoot: Offset): Boolean = isExcludedFromDoubleTap(pointInRoot, regions.values)
}

internal fun isExcludedFromDoubleTap(point: Offset, regions: Collection<Rect>): Boolean =
    regions.any { it.contains(point) }

internal fun Modifier.excludeFromDoubleTap(exclusions: DoubleTapExclusions, key: Any): Modifier = composed {
    DisposableEffect(exclusions, key) {
        onDispose { exclusions.remove(key) }
    }
    Modifier.onGloballyPositioned { exclusions.update(key, it.boundsInRoot()) }
}

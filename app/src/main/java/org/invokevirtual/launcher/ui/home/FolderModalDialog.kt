package org.invokevirtual.launcher.ui.home

import android.content.ComponentName
import android.graphics.Rect
import android.os.Process
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.FolderItem
import org.invokevirtual.launcher.data.FolderLayoutMode
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppProfile
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.NotificationBadgeBox
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.rememberAnimatedContainerMorphShape
import org.invokevirtual.launcher.ui.theme.restPolygon
import java.util.UUID
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed

@Immutable
data class FolderSheetActions(
    val onManageApps: () -> Unit = {},
    val onSetLayout: (FolderLayoutMode, Int) -> Unit = { _, _ -> },
    val onAddSpacer: (String, TileSpan, Int?, Int?) -> Unit = { _, _, _, _ -> },
    val onRemoveItem: (String) -> Unit = {},
    val onMoveItem: (String, Int) -> Unit = { _, _ -> },
    val onSetItemSpan: (String, TileSpan) -> Unit = { _, _ -> },
    val onSetItemCustomSize: (String, Int, Int) -> Unit = { _, _, _ -> },
    val onSetItemShape: (String, TileShapePreset) -> Unit = { _, _ -> },
    val onSetItemTone: (String, TileTone) -> Unit = { _, _ -> },
    val onSetItemCustomBgColor: (String, Int?) -> Unit = { _, _ -> },
)

@Composable
fun FolderBottomSheet(
    folder: CanvasBlock.FolderTile,
    appsByKey: Map<AppKey, InstalledApp>,
    iconStyle: IconStyle,
    showLabels: Boolean,
    appCatalog: AppCatalog?,
    onDismiss: () -> Unit,
    actions: FolderSheetActions,
    onLaunchApp: (InstalledApp, Rect?) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        FolderSheetContent(
            folder = folder,
            appsByKey = appsByKey,
            iconStyle = iconStyle,
            showLabels = showLabels,
            appCatalog = appCatalog,
            actions = actions,
            onLaunchApp = onLaunchApp,
            onLongPressApp = onLongPressApp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        )
    }
}

internal sealed interface BentoExtraRow<out T> {
    data class FullWide<T>(val app: T) : BentoExtraRow<T>
    data class TwoHalves<T>(val first: T, val second: T) : BentoExtraRow<T>
    data class WideAndTwoCompact<T>(
        val wide: T,
        val compactA: T,
        val compactB: T,
        val wideOnLeft: Boolean,
    ) : BentoExtraRow<T>

    data class FourCompact<T>(val apps: List<T>) : BentoExtraRow<T>
}

internal fun <T> buildBentoExtraRows(remaining: List<T>): List<BentoExtraRow<T>> {
    if (remaining.isEmpty()) return emptyList()
    val rows = mutableListOf<BentoExtraRow<T>>()
    var index = 0
    var rowIdx = 0
    var nextWideLeft = true
    while (index < remaining.size) {
        when (remaining.size - index) {
            1 -> {
                rows.add(BentoExtraRow.FullWide(remaining[index]))
                index += 1
            }

            2 -> {
                rows.add(BentoExtraRow.TwoHalves(remaining[index], remaining[index + 1]))
                index += 2
            }

            3 -> {
                rows.add(
                    BentoExtraRow.WideAndTwoCompact(
                        wide = remaining[index],
                        compactA = remaining[index + 1],
                        compactB = remaining[index + 2],
                        wideOnLeft = nextWideLeft,
                    ),
                )
                nextWideLeft = !nextWideLeft
                index += 3
            }

            4 -> {
                rows.add(BentoExtraRow.FourCompact(remaining.subList(index, index + 4)))
                index += 4
            }

            else -> {
                if (rowIdx % 2 == 0) {
                    rows.add(
                        BentoExtraRow.WideAndTwoCompact(
                            wide = remaining[index],
                            compactA = remaining[index + 1],
                            compactB = remaining[index + 2],
                            wideOnLeft = nextWideLeft,
                        ),
                    )
                    nextWideLeft = !nextWideLeft
                    index += 3
                } else {
                    rows.add(BentoExtraRow.FourCompact(remaining.subList(index, index + 4)))
                    index += 4
                }
            }
        }
        rowIdx += 1
    }
    return rows
}

internal sealed interface BentoRenderSegment<out A> {
    data class Spacer(val item: FolderItem.SpacerItem) : BentoRenderSegment<Nothing>
    data class Cluster<A>(val apps: List<A>) : BentoRenderSegment<A>
}

internal fun <A> buildBentoRenderSegments(
    items: List<FolderItem>,
    resolveApp: (FolderItem.AppItem) -> A?,
): List<BentoRenderSegment<A>> {
    if (items.isEmpty()) return emptyList()
    val segments = mutableListOf<BentoRenderSegment<A>>()
    val currentApps = mutableListOf<A>()

    fun flushApps() {
        if (currentApps.isNotEmpty()) {
            segments.add(BentoRenderSegment.Cluster(currentApps.toList()))
            currentApps.clear()
        }
    }

    items.forEach { item ->
        when (item) {
            is FolderItem.SpacerItem -> {
                flushApps()
                segments.add(BentoRenderSegment.Spacer(item))
            }

            is FolderItem.AppItem -> {
                resolveApp(item)?.let { resolved ->
                    currentApps.add(resolved)
                }
            }
        }
    }
    flushApps()
    return segments
}

private data class ResolvedBentoApp(
    val item: FolderItem.AppItem,
    val app: InstalledApp,
)

@Composable
internal fun FolderSheetContent(
    folder: CanvasBlock.FolderTile,
    appsByKey: Map<AppKey, InstalledApp>,
    iconStyle: IconStyle,
    showLabels: Boolean,
    appCatalog: AppCatalog?,
    actions: FolderSheetActions,
    onLaunchApp: (InstalledApp, Rect?) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    initialEditMode: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    var isEditing by rememberSaveable(folder.id) { mutableStateOf(initialEditMode) }
    var selectedItemId by rememberSaveable(folder.id) { mutableStateOf<String?>(null) }
    var pendingSelectItemId by remember(folder.id) { mutableStateOf<String?>(null) }
    var liveResizePreview by remember(folder.id) {
        mutableStateOf<Pair<String, Pair<Int, Int>>?>(
            null
        )
    }

    val effectiveItems = remember(folder.id, folder.apps, folder.items, folder.shape) {
        folder.effectiveItems
    }

    val visibleItems = remember(effectiveItems, appsByKey) {
        effectiveItems.filter { item ->
            when (item) {
                is FolderItem.SpacerItem -> true
                is FolderItem.AppItem -> item.appKey in appsByKey
            }
        }
    }

    LaunchedEffect(visibleItems, pendingSelectItemId) {
        val pending = pendingSelectItemId ?: return@LaunchedEffect
        if (visibleItems.any { it.id == pending }) {
            selectedItemId = pending
            pendingSelectItemId = null
        }
    }

    LaunchedEffect(isEditing) {
        if (!isEditing) {
            selectedItemId = null
            liveResizePreview = null
        }
    }

    LaunchedEffect(visibleItems) {
        val selected = selectedItemId ?: return@LaunchedEffect
        if (visibleItems.none { it.id == selected }) {
            selectedItemId = null
        }
    }

    val appCount = remember(visibleItems) {
        visibleItems.count { it is FolderItem.AppItem }
    }
    val selectedIndex = visibleItems.indexOfFirst { it.id == selectedItemId }
    val selectedItem = visibleItems.getOrNull(selectedIndex)

    val folderBadgeShape = rememberAnimatedContainerMorphShape(
        shapePreset = folder.shape,
        aspectRatio = 1f,
        pressed = false,
        reducedMotion = reducedMotion,
    )

    val customColor = folder.customBgArgb?.let(::Color)
    val badgeContainerColor = when {
        customColor != null -> customColor
        folder.tone != TileTone.SURFACE -> folder.tone.containerColor().copy(alpha = 1f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val badgeContentColor = when {
        customColor != null -> contrastingForegroundFor(customColor)
        folder.tone != TileTone.SURFACE -> folder.tone.contentColor()
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val layoutLabel = when (folder.layoutMode) {
        FolderLayoutMode.BENTO -> "Bento"
        FolderLayoutMode.GRID -> "${folder.effectiveGridColumns}× Grid"
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(folderBadgeShape)
                    .background(badgeContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = InvokeIcons.Folder,
                    contentDescription = null,
                    tint = badgeContentColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.title.ifBlank { "Folder" },
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        isEditing -> "Editing grid · $layoutLabel"
                        appCount == 0 -> "Empty folder · $layoutLabel"
                        appCount == 1 -> "1 app · $layoutLabel"
                        else -> "$appCount apps · $layoutLabel"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isEditing = !isEditing
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isEditing) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (isEditing) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    imageVector = if (isEditing) InvokeIcons.Check else InvokeIcons.Edit,
                    contentDescription = if (isEditing) "Done editing folder" else "Edit folder grid",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        AnimatedVisibility(visible = isEditing) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item(key = "layout_bento") {
                    FilterChip(
                        selected = folder.layoutMode == FolderLayoutMode.BENTO,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onSetLayout(FolderLayoutMode.BENTO, folder.effectiveGridColumns)
                        },
                        label = { Text("Bento") },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
                items(3, key = { idx -> "layout_cols_${idx + 2}" }) { idx ->
                    val cols = idx + 2
                    FilterChip(
                        selected = folder.layoutMode == FolderLayoutMode.GRID && folder.effectiveGridColumns == cols,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onSetLayout(FolderLayoutMode.GRID, cols)
                        },
                        label = { Text("$cols Col") },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
                item(key = "folder_add_space") {
                    FilterChip(
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val newId = "fspc_${UUID.randomUUID().toString().take(8)}"
                            pendingSelectItemId = newId
                            selectedItemId = newId
                            val defaultSpan =
                                if (folder.layoutMode == FolderLayoutMode.GRID && folder.effectiveGridColumns == 2) {
                                    TileSpan.WIDE
                                } else {
                                    TileSpan.BANNER
                                }
                            actions.onAddSpacer(newId, defaultSpan, null, null)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = InvokeIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = { Text("Space") },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
                item(key = "folder_manage_apps") {
                    FilterChip(
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onManageApps()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = InvokeIcons.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = { Text("Apps") },
                        modifier = Modifier.heightIn(min = 38.dp),
                    )
                }
            }
        }

        if (visibleItems.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "No apps in this folder yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onManageApps()
                        },
                        shape = RoundedCornerShape(percent = 50),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("Add apps")
                    }
                }
            }
        } else if (folder.layoutMode == FolderLayoutMode.GRID) {
            val gridColumns = folder.effectiveGridColumns
            val gridMaxHeight = if (isEditing && selectedItem != null) 380.dp else 520.dp
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = gridMaxHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                gridItemsIndexed(
                    items = visibleItems,
                    key = { _, item -> item.id },
                    span = { _, item ->
                        val overrideCols = liveResizePreview
                            ?.takeIf { it.first == item.id }
                            ?.second
                            ?.first
                        val cols = when (item) {
                            is FolderItem.AppItem -> overrideCols ?: item.effectiveColumns(
                                gridColumns
                            )

                            is FolderItem.SpacerItem -> overrideCols ?: item.effectiveColumns(
                                gridColumns
                            )
                        }.coerceIn(1, maxLineSpan)
                        GridItemSpan(cols)
                    },
                ) { _, item ->
                    val isSelected = isEditing && item.id == selectedItemId
                    val previewOverride = liveResizePreview
                        ?.takeIf { it.first == item.id }
                        ?.second

                    when (item) {
                        is FolderItem.AppItem -> {
                            val resolvedApp = appsByKey[item.appKey] ?: return@gridItemsIndexed
                            val displayItem = if (previewOverride != null) {
                                item.copy(
                                    customColumns = previewOverride.first,
                                    customHeightDp = previewOverride.second,
                                )
                            } else {
                                item
                            }
                            FolderGridAppTileCard(
                                item = displayItem,
                                app = resolvedApp,
                                gridColumns = gridColumns,
                                iconStyle = iconStyle,
                                showLabel = showLabels,
                                isEditMode = isEditing,
                                isSelected = isSelected,
                                appCatalog = appCatalog,
                                onClick = { bounds -> onLaunchApp(resolvedApp, bounds) },
                                onLongClick = {
                                    if (isEditing) {
                                        selectedItemId = item.id
                                    } else {
                                        onLongPressApp(resolvedApp)
                                    }
                                },
                                onSelectInEdit = { selectedItemId = item.id },
                            )
                        }

                        is FolderItem.SpacerItem -> {
                            val displayItem = if (previewOverride != null) {
                                item.copy(
                                    customColumns = previewOverride.first,
                                    customHeightDp = previewOverride.second,
                                )
                            } else {
                                item
                            }
                            CanvasSpacerPlaceholderCard(
                                sizeLabel = displayItem.sizeLabel(gridColumns),
                                isSquareSize = displayItem.isSquareSize,
                                effectiveHeightDp = displayItem.effectiveHeightDp,
                                isEditMode = isEditing,
                                isSelected = isSelected,
                                onSelectInEdit = { selectedItemId = item.id },
                            )
                        }
                    }
                }
            }
        } else {
            val renderSegments = remember(visibleItems, appsByKey) {
                buildBentoRenderSegments(visibleItems) { item ->
                    appsByKey[item.appKey]?.let { ResolvedBentoApp(item, it) }
                }
            }
            val listMaxHeight = if (isEditing && selectedItem != null) 380.dp else 520.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                renderSegments.forEachIndexed { segIdx, segment ->
                    when (segment) {
                        is BentoRenderSegment.Spacer -> {
                            item(key = "bento_spacer_${segment.item.id}") {
                                val previewOverride = liveResizePreview
                                    ?.takeIf { it.first == segment.item.id }
                                    ?.second
                                val displayItem = if (previewOverride != null) {
                                    segment.item.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    segment.item
                                }
                                CanvasSpacerPlaceholderCard(
                                    sizeLabel = displayItem.sizeLabel(4),
                                    isSquareSize = false,
                                    effectiveHeightDp = displayItem.effectiveHeightDp,
                                    isEditMode = isEditing,
                                    isSelected = isEditing && segment.item.id == selectedItemId,
                                    onSelectInEdit = { selectedItemId = segment.item.id },
                                )
                            }
                        }

                        is BentoRenderSegment.Cluster -> {
                            val clusterApps = segment.apps
                            when (clusterApps.size) {
                                1 -> {
                                    item(key = "bento_single_${segIdx}_${clusterApps[0].item.id}") {
                                        val entry = clusterApps[0]
                                        val (cardBg, cardFg) = resolveBentoColors(
                                            item = entry.item,
                                            defaultBg = badgeContainerColor,
                                            defaultFg = badgeContentColor,
                                        )
                                        BentoHorizontalAppCard(
                                            app = entry.app,
                                            shapePreset = entry.item.shape,
                                            iconStyle = iconStyle,
                                            appCatalog = appCatalog,
                                            containerColor = cardBg,
                                            contentColor = cardFg,
                                            cornerRadius = 26.dp,
                                            iconSize = 54.dp,
                                            subtitle = if (entry.app.isWorkProfile) "Work profile" else "Tap to launch",
                                            isEditMode = isEditing,
                                            isSelected = isEditing && entry.item.id == selectedItemId,
                                            onClick = { bounds ->
                                                if (isEditing) {
                                                    selectedItemId = entry.item.id
                                                } else {
                                                    onLaunchApp(entry.app, bounds)
                                                }
                                            },
                                            onLongClick = {
                                                if (isEditing) {
                                                    selectedItemId = entry.item.id
                                                } else {
                                                    onLongPressApp(entry.app)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp),
                                        )
                                    }
                                }

                                2 -> {
                                    item(key = "bento_pair_${segIdx}_${clusterApps[0].item.id}") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            clusterApps.forEachIndexed { idx, entry ->
                                                val (cardBg, cardFg) = resolveBentoColors(
                                                    item = entry.item,
                                                    defaultBg = if (idx == 0) badgeContainerColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    defaultFg = if (idx == 0) badgeContentColor else MaterialTheme.colorScheme.onSurface,
                                                )
                                                BentoVerticalAppCard(
                                                    app = entry.app,
                                                    shapePreset = entry.item.shape,
                                                    iconStyle = iconStyle,
                                                    appCatalog = appCatalog,
                                                    containerColor = cardBg,
                                                    contentColor = cardFg,
                                                    cornerRadius = 26.dp,
                                                    iconSize = 52.dp,
                                                    isEditMode = isEditing,
                                                    isSelected = isEditing && entry.item.id == selectedItemId,
                                                    onClick = { bounds ->
                                                        if (isEditing) {
                                                            selectedItemId = entry.item.id
                                                        } else {
                                                            onLaunchApp(entry.app, bounds)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (isEditing) {
                                                            selectedItemId = entry.item.id
                                                        } else {
                                                            onLongPressApp(entry.app)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(122.dp),
                                                )
                                            }
                                        }
                                    }
                                }

                                3 -> {
                                    item(key = "bento_trio_${segIdx}_${clusterApps[0].item.id}") {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(168.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            val first = clusterApps[0]
                                            val (firstBg, firstFg) = resolveBentoColors(
                                                item = first.item,
                                                defaultBg = badgeContainerColor,
                                                defaultFg = badgeContentColor,
                                            )
                                            BentoVerticalAppCard(
                                                app = first.app,
                                                shapePreset = first.item.shape,
                                                iconStyle = iconStyle,
                                                appCatalog = appCatalog,
                                                containerColor = firstBg,
                                                contentColor = firstFg,
                                                cornerRadius = 28.dp,
                                                iconSize = 60.dp,
                                                isEditMode = isEditing,
                                                isSelected = isEditing && first.item.id == selectedItemId,
                                                onClick = { bounds ->
                                                    if (isEditing) {
                                                        selectedItemId = first.item.id
                                                    } else {
                                                        onLaunchApp(first.app, bounds)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (isEditing) {
                                                        selectedItemId = first.item.id
                                                    } else {
                                                        onLongPressApp(first.app)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                listOf(
                                                    clusterApps[1],
                                                    clusterApps[2]
                                                ).forEachIndexed { idx, entry ->
                                                    val (cardBg, cardFg) = resolveBentoColors(
                                                        item = entry.item,
                                                        defaultBg = if (idx == 0) {
                                                            MaterialTheme.colorScheme.surfaceContainerHighest
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                                        },
                                                        defaultFg = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    BentoHorizontalAppCard(
                                                        app = entry.app,
                                                        shapePreset = entry.item.shape,
                                                        iconStyle = iconStyle,
                                                        appCatalog = appCatalog,
                                                        containerColor = cardBg,
                                                        contentColor = cardFg,
                                                        cornerRadius = 22.dp,
                                                        iconSize = 38.dp,
                                                        isEditMode = isEditing,
                                                        isSelected = isEditing && entry.item.id == selectedItemId,
                                                        onClick = { bounds ->
                                                            if (isEditing) {
                                                                selectedItemId = entry.item.id
                                                            } else {
                                                                onLaunchApp(entry.app, bounds)
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (isEditing) {
                                                                selectedItemId = entry.item.id
                                                            } else {
                                                                onLongPressApp(entry.app)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .weight(1f),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    item(key = "bento_hero_cluster_${segIdx}_${clusterApps[0].item.id}") {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(172.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            val first = clusterApps[0]
                                            val (firstBg, firstFg) = resolveBentoColors(
                                                item = first.item,
                                                defaultBg = badgeContainerColor,
                                                defaultFg = badgeContentColor,
                                            )
                                            BentoVerticalAppCard(
                                                app = first.app,
                                                shapePreset = first.item.shape,
                                                iconStyle = iconStyle,
                                                appCatalog = appCatalog,
                                                containerColor = firstBg,
                                                contentColor = firstFg,
                                                cornerRadius = 28.dp,
                                                iconSize = 60.dp,
                                                isEditMode = isEditing,
                                                isSelected = isEditing && first.item.id == selectedItemId,
                                                onClick = { bounds ->
                                                    if (isEditing) {
                                                        selectedItemId = first.item.id
                                                    } else {
                                                        onLaunchApp(first.app, bounds)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (isEditing) {
                                                        selectedItemId = first.item.id
                                                    } else {
                                                        onLongPressApp(first.app)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                            )
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                val second = clusterApps[1]
                                                val (secondBg, secondFg) = resolveBentoColors(
                                                    item = second.item,
                                                    defaultBg = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                    defaultFg = MaterialTheme.colorScheme.onSurface,
                                                )
                                                BentoHorizontalAppCard(
                                                    app = second.app,
                                                    shapePreset = second.item.shape,
                                                    iconStyle = iconStyle,
                                                    appCatalog = appCatalog,
                                                    containerColor = secondBg,
                                                    contentColor = secondFg,
                                                    cornerRadius = 22.dp,
                                                    iconSize = 38.dp,
                                                    isEditMode = isEditing,
                                                    isSelected = isEditing && second.item.id == selectedItemId,
                                                    onClick = { bounds ->
                                                        if (isEditing) {
                                                            selectedItemId = second.item.id
                                                        } else {
                                                            onLaunchApp(second.app, bounds)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (isEditing) {
                                                            selectedItemId = second.item.id
                                                        } else {
                                                            onLongPressApp(second.app)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    listOf(
                                                        clusterApps[2],
                                                        clusterApps[3]
                                                    ).forEach { entry ->
                                                        val (cardBg, cardFg) = resolveBentoColors(
                                                            item = entry.item,
                                                            defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            defaultFg = MaterialTheme.colorScheme.onSurface,
                                                        )
                                                        BentoCompactAppCard(
                                                            app = entry.app,
                                                            shapePreset = entry.item.shape,
                                                            iconStyle = iconStyle,
                                                            appCatalog = appCatalog,
                                                            containerColor = cardBg,
                                                            contentColor = cardFg,
                                                            isEditMode = isEditing,
                                                            isSelected = isEditing && entry.item.id == selectedItemId,
                                                            onClick = { bounds ->
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLaunchApp(entry.app, bounds)
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLongPressApp(entry.app)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val extraRows = buildBentoExtraRows(clusterApps.drop(4))
                                    itemsIndexed(
                                        items = extraRows,
                                        key = { idx, _ -> "bento_extra_${segIdx}_$idx" },
                                    ) { _, row ->
                                        when (row) {
                                            is BentoExtraRow.FullWide -> {
                                                val entry = row.app
                                                val (cardBg, cardFg) = resolveBentoColors(
                                                    item = entry.item,
                                                    defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    defaultFg = MaterialTheme.colorScheme.onSurface,
                                                )
                                                BentoHorizontalAppCard(
                                                    app = entry.app,
                                                    shapePreset = entry.item.shape,
                                                    iconStyle = iconStyle,
                                                    appCatalog = appCatalog,
                                                    containerColor = cardBg,
                                                    contentColor = cardFg,
                                                    cornerRadius = 22.dp,
                                                    iconSize = 42.dp,
                                                    isEditMode = isEditing,
                                                    isSelected = isEditing && entry.item.id == selectedItemId,
                                                    onClick = { bounds ->
                                                        if (isEditing) {
                                                            selectedItemId = entry.item.id
                                                        } else {
                                                            onLaunchApp(entry.app, bounds)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (isEditing) {
                                                            selectedItemId = entry.item.id
                                                        } else {
                                                            onLongPressApp(entry.app)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(76.dp),
                                                )
                                            }

                                            is BentoExtraRow.TwoHalves -> {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    listOf(row.first, row.second).forEach { entry ->
                                                        val (cardBg, cardFg) = resolveBentoColors(
                                                            item = entry.item,
                                                            defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            defaultFg = MaterialTheme.colorScheme.onSurface,
                                                        )
                                                        BentoHorizontalAppCard(
                                                            app = entry.app,
                                                            shapePreset = entry.item.shape,
                                                            iconStyle = iconStyle,
                                                            appCatalog = appCatalog,
                                                            containerColor = cardBg,
                                                            contentColor = cardFg,
                                                            cornerRadius = 22.dp,
                                                            iconSize = 38.dp,
                                                            isEditMode = isEditing,
                                                            isSelected = isEditing && entry.item.id == selectedItemId,
                                                            onClick = { bounds ->
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLaunchApp(entry.app, bounds)
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLongPressApp(entry.app)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(78.dp),
                                                        )
                                                    }
                                                }
                                            }

                                            is BentoExtraRow.WideAndTwoCompact -> {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(84.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    val wideEntry = row.wide
                                                    val (wideBg, wideFg) = resolveBentoColors(
                                                        item = wideEntry.item,
                                                        defaultBg = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        defaultFg = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    val compactEntries =
                                                        listOf(row.compactA, row.compactB)
                                                    if (row.wideOnLeft) {
                                                        BentoHorizontalAppCard(
                                                            app = wideEntry.app,
                                                            shapePreset = wideEntry.item.shape,
                                                            iconStyle = iconStyle,
                                                            appCatalog = appCatalog,
                                                            containerColor = wideBg,
                                                            contentColor = wideFg,
                                                            cornerRadius = 22.dp,
                                                            iconSize = 40.dp,
                                                            isEditMode = isEditing,
                                                            isSelected = isEditing && wideEntry.item.id == selectedItemId,
                                                            onClick = { bounds ->
                                                                if (isEditing) {
                                                                    selectedItemId =
                                                                        wideEntry.item.id
                                                                } else {
                                                                    onLaunchApp(
                                                                        wideEntry.app,
                                                                        bounds
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isEditing) {
                                                                    selectedItemId =
                                                                        wideEntry.item.id
                                                                } else {
                                                                    onLongPressApp(wideEntry.app)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(2f)
                                                                .fillMaxHeight(),
                                                        )
                                                        compactEntries.forEach { entry ->
                                                            val (cardBg, cardFg) = resolveBentoColors(
                                                                item = entry.item,
                                                                defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                                defaultFg = MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            BentoCompactAppCard(
                                                                app = entry.app,
                                                                shapePreset = entry.item.shape,
                                                                iconStyle = iconStyle,
                                                                appCatalog = appCatalog,
                                                                containerColor = cardBg,
                                                                contentColor = cardFg,
                                                                isEditMode = isEditing,
                                                                isSelected = isEditing && entry.item.id == selectedItemId,
                                                                onClick = { bounds ->
                                                                    if (isEditing) {
                                                                        selectedItemId =
                                                                            entry.item.id
                                                                    } else {
                                                                        onLaunchApp(
                                                                            entry.app,
                                                                            bounds
                                                                        )
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    if (isEditing) {
                                                                        selectedItemId =
                                                                            entry.item.id
                                                                    } else {
                                                                        onLongPressApp(entry.app)
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .fillMaxHeight(),
                                                            )
                                                        }
                                                    } else {
                                                        compactEntries.forEach { entry ->
                                                            val (cardBg, cardFg) = resolveBentoColors(
                                                                item = entry.item,
                                                                defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                                defaultFg = MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            BentoCompactAppCard(
                                                                app = entry.app,
                                                                shapePreset = entry.item.shape,
                                                                iconStyle = iconStyle,
                                                                appCatalog = appCatalog,
                                                                containerColor = cardBg,
                                                                contentColor = cardFg,
                                                                isEditMode = isEditing,
                                                                isSelected = isEditing && entry.item.id == selectedItemId,
                                                                onClick = { bounds ->
                                                                    if (isEditing) {
                                                                        selectedItemId =
                                                                            entry.item.id
                                                                    } else {
                                                                        onLaunchApp(
                                                                            entry.app,
                                                                            bounds
                                                                        )
                                                                    }
                                                                },
                                                                onLongClick = {
                                                                    if (isEditing) {
                                                                        selectedItemId =
                                                                            entry.item.id
                                                                    } else {
                                                                        onLongPressApp(entry.app)
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .fillMaxHeight(),
                                                            )
                                                        }
                                                        BentoHorizontalAppCard(
                                                            app = wideEntry.app,
                                                            shapePreset = wideEntry.item.shape,
                                                            iconStyle = iconStyle,
                                                            appCatalog = appCatalog,
                                                            containerColor = wideBg,
                                                            contentColor = wideFg,
                                                            cornerRadius = 22.dp,
                                                            iconSize = 40.dp,
                                                            isEditMode = isEditing,
                                                            isSelected = isEditing && wideEntry.item.id == selectedItemId,
                                                            onClick = { bounds ->
                                                                if (isEditing) {
                                                                    selectedItemId =
                                                                        wideEntry.item.id
                                                                } else {
                                                                    onLaunchApp(
                                                                        wideEntry.app,
                                                                        bounds
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isEditing) {
                                                                    selectedItemId =
                                                                        wideEntry.item.id
                                                                } else {
                                                                    onLongPressApp(wideEntry.app)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(2f)
                                                                .fillMaxHeight(),
                                                        )
                                                    }
                                                }
                                            }

                                            is BentoExtraRow.FourCompact -> {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(86.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    row.apps.forEach { entry ->
                                                        val (cardBg, cardFg) = resolveBentoColors(
                                                            item = entry.item,
                                                            defaultBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            defaultFg = MaterialTheme.colorScheme.onSurface,
                                                        )
                                                        BentoCompactAppCard(
                                                            app = entry.app,
                                                            shapePreset = entry.item.shape,
                                                            iconStyle = iconStyle,
                                                            appCatalog = appCatalog,
                                                            containerColor = cardBg,
                                                            contentColor = cardFg,
                                                            isEditMode = isEditing,
                                                            isSelected = isEditing && entry.item.id == selectedItemId,
                                                            onClick = { bounds ->
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLaunchApp(entry.app, bounds)
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isEditing) {
                                                                    selectedItemId = entry.item.id
                                                                } else {
                                                                    onLongPressApp(entry.app)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = isEditing && selectedItem != null) {
            selectedItem?.let { activeItem ->
                val gridCols = if (folder.layoutMode == FolderLayoutMode.GRID) {
                    folder.effectiveGridColumns
                } else {
                    4
                }
                val previewOverride = liveResizePreview
                    ?.takeIf { it.first == activeItem.id }
                    ?.second
                val itemTitle = when (activeItem) {
                    is FolderItem.AppItem -> appsByKey[activeItem.appKey]?.label ?: "App"
                    is FolderItem.SpacerItem -> "Empty Space"
                }
                val itemSizeLabel = when (activeItem) {
                    is FolderItem.AppItem -> if (previewOverride != null) {
                        "${previewOverride.first}×${previewOverride.second}dp"
                    } else {
                        activeItem.sizeLabel(gridCols)
                    }

                    is FolderItem.SpacerItem -> if (previewOverride != null) {
                        "${previewOverride.first}×${previewOverride.second}dp"
                    } else {
                        activeItem.sizeLabel(gridCols)
                    }
                }
                val currentSpan = when (activeItem) {
                    is FolderItem.AppItem -> activeItem.span
                    is FolderItem.SpacerItem -> activeItem.span
                }
                val activeCols = when (activeItem) {
                    is FolderItem.AppItem -> activeItem.effectiveColumns(gridCols)
                    is FolderItem.SpacerItem -> activeItem.effectiveColumns(gridCols)
                }
                val activeHeight = when (activeItem) {
                    is FolderItem.AppItem -> activeItem.effectiveHeightDp
                    is FolderItem.SpacerItem -> activeItem.effectiveHeightDp
                }
                val activeHasCustom = when (activeItem) {
                    is FolderItem.AppItem -> activeItem.hasCustomSize
                    is FolderItem.SpacerItem -> activeItem.hasCustomSize
                }
                val appItem = activeItem as? FolderItem.AppItem

                CanvasBlockInspectorBar(
                    blockId = activeItem.id,
                    title = itemTitle,
                    sizeLabel = itemSizeLabel,
                    currentShape = appItem?.shape ?: TileShapePreset.SQUIRCLE,
                    currentTileSpan = currentSpan,
                    currentWidgetSize = null,
                    currentWidgetColumns = previewOverride?.first ?: activeCols,
                    currentWidgetHeightDp = previewOverride?.second ?: activeHeight,
                    hasCustomWidgetSize = previewOverride != null || activeHasCustom,
                    currentTone = appItem?.tone,
                    currentCustomBgArgb = appItem?.customBgArgb,
                    canMoveBack = selectedIndex > 0,
                    canMoveForward = selectedIndex in 0 until visibleItems.lastIndex,
                    supportsShape = appItem != null,
                    maxColumns = gridCols,
                    minCustomTileHeightDp = if (appItem == null) 24f else 64f,
                    maxCustomTileHeightDp = if (appItem == null) 320f else 280f,
                    onMoveBack = { actions.onMoveItem(activeItem.id, -1) },
                    onMoveForward = { actions.onMoveItem(activeItem.id, +1) },
                    onSelectTileSpan = { span ->
                        liveResizePreview = null
                        if (appItem != null && folder.layoutMode == FolderLayoutMode.BENTO) {
                            actions.onSetLayout(FolderLayoutMode.GRID, folder.effectiveGridColumns)
                        }
                        actions.onSetItemSpan(activeItem.id, span)
                    },
                    onSelectWidgetSize = {},
                    onPreviewWidgetCustomSize = { cols, heightDp ->
                        liveResizePreview = activeItem.id to (cols to heightDp)
                    },
                    onSelectWidgetCustomSize = { cols, heightDp ->
                        liveResizePreview = null
                        if (appItem != null && folder.layoutMode == FolderLayoutMode.BENTO) {
                            actions.onSetLayout(FolderLayoutMode.GRID, folder.effectiveGridColumns)
                        }
                        actions.onSetItemCustomSize(activeItem.id, cols, heightDp)
                    },
                    onSelectShape = { shape ->
                        actions.onSetItemShape(activeItem.id, shape)
                    },
                    onSelectTone = { tone ->
                        actions.onSetItemTone(activeItem.id, tone)
                    },
                    onSelectCustomBgArgb = { argb ->
                        actions.onSetItemCustomBgColor(activeItem.id, argb)
                    },
                    onRemove = {
                        liveResizePreview = null
                        actions.onRemoveItem(activeItem.id)
                        selectedItemId = null
                    },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun resolveBentoColors(
    item: FolderItem.AppItem,
    defaultBg: Color,
    defaultFg: Color,
): Pair<Color, Color> {
    val customColor = item.customBgArgb?.let(::Color)
    return when {
        customColor != null -> customColor to contrastingForegroundFor(customColor)
        item.tone != TileTone.SURFACE -> item.tone.containerColor() to item.tone.contentColor()
        else -> defaultBg to defaultFg
    }
}

@Composable
private fun FolderGridAppTileCard(
    item: FolderItem.AppItem,
    app: InstalledApp,
    gridColumns: Int,
    iconStyle: IconStyle,
    showLabel: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    appCatalog: AppCatalog?,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    val effectiveCols = item.effectiveColumns(gridColumns)
    val customColor = item.customBgArgb?.let(::Color)
    val containerColor = customColor ?: item.tone.containerColor()
    val contentColor = customColor?.let(::contrastingForegroundFor) ?: item.tone.contentColor()
    val isFloating = effectiveCols == 1 &&
            (if (item.hasCustomSize) item.effectiveHeightDp <= 116 && item.customBgArgb == null else item.span == TileSpan.COMPACT)

    CanvasTileScaffold(
        span = item.span,
        effectiveColumns = effectiveCols,
        effectiveHeightDp = item.effectiveHeightDp,
        isSquareSize = item.isSquareSize,
        hasCustomSize = item.hasCustomSize,
        shapePreset = item.shape,
        label = app.label,
        subLabel = null,
        showLabel = showLabel,
        hasCustomBg = item.customBgArgb != null,
        containerColor = containerColor,
        contentColor = contentColor,
        isEditMode = isEditMode,
        isSelected = isSelected,
        accessibilityLabel = app.label,
        accessibilityActionLabel = "App actions",
        onClick = onClick,
        onLongClick = onLongClick,
        onSelectInEdit = onSelectInEdit,
        modifier = modifier,
        iconContent = { iconSize, morphShape ->
            val isContainerTile = !isFloating
            NotificationBadgeBox(apps = listOf(app.key), iconSize = iconSize) {
                ShapedAppIcon(
                    iconAsset = iconAsset,
                    fallbackGlyph = InvokeIcons.Apps,
                    iconStyle = iconStyle,
                    shape = if (isContainerTile) RoundedCornerShape(percent = 26) else morphShape,
                    size = iconSize,
                    monochromeBg = if (isContainerTile) contentColor.copy(alpha = 0.11f) else containerColor,
                    monochromeFg = contentColor,
                    forceContainerBg = !isContainerTile && item.customBgArgb != null,
                )
            }
        },
    )
}

@Composable
private fun BentoVerticalAppCard(
    app: InstalledApp,
    shapePreset: TileShapePreset,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    containerColor: Color,
    contentColor: Color,
    cornerRadius: Dp,
    iconSize: Dp,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.02f
            pressed -> 0.95f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "bentoVertPress",
    )
    val iconShape = rememberAnimatedContainerMorphShape(
        shapePreset = shapePreset,
        aspectRatio = 1f,
        pressed = pressed && !isEditMode,
        reducedMotion = reducedMotion,
    )
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    var bounds by remember { mutableStateOf<Rect?>(null) }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        border = when {
            isEditMode && isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isEditMode -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            )

            else -> null
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                bounds = Rect(
                    b.left.roundToInt(),
                    b.top.roundToInt(),
                    b.right.roundToInt(),
                    b.bottom.roundToInt(),
                )
            }
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(bounds) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            NotificationBadgeBox(apps = listOf(app.key), iconSize = iconSize) {
                ShapedAppIcon(
                    iconAsset = iconAsset,
                    fallbackGlyph = InvokeIcons.Apps,
                    iconStyle = iconStyle,
                    shape = iconShape,
                    size = iconSize,
                    monochromeBg = contentColor.copy(alpha = 0.14f),
                    monochromeFg = contentColor,
                )
            }
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BentoHorizontalAppCard(
    app: InstalledApp,
    shapePreset: TileShapePreset,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    containerColor: Color,
    contentColor: Color,
    cornerRadius: Dp,
    iconSize: Dp,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.02f
            pressed -> 0.96f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "bentoHorizPress",
    )
    val iconShape = rememberAnimatedContainerMorphShape(
        shapePreset = shapePreset,
        aspectRatio = 1f,
        pressed = pressed && !isEditMode,
        reducedMotion = reducedMotion,
    )
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    var bounds by remember { mutableStateOf<Rect?>(null) }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        border = when {
            isEditMode && isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isEditMode -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            )

            else -> null
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                bounds = Rect(
                    b.left.roundToInt(),
                    b.top.roundToInt(),
                    b.right.roundToInt(),
                    b.bottom.roundToInt(),
                )
            }
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(bounds) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotificationBadgeBox(apps = listOf(app.key), iconSize = iconSize) {
                ShapedAppIcon(
                    iconAsset = iconAsset,
                    fallbackGlyph = InvokeIcons.Apps,
                    iconStyle = iconStyle,
                    shape = iconShape,
                    size = iconSize,
                    monochromeBg = contentColor.copy(alpha = 0.14f),
                    monochromeFg = contentColor,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.74f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoCompactAppCard(
    app: InstalledApp,
    shapePreset: TileShapePreset,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    containerColor: Color,
    contentColor: Color,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.03f
            pressed -> 0.94f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "bentoCompactPress",
    )
    val iconShape = rememberAnimatedContainerMorphShape(
        shapePreset = shapePreset,
        aspectRatio = 1f,
        pressed = pressed && !isEditMode,
        reducedMotion = reducedMotion,
    )
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    var bounds by remember { mutableStateOf<Rect?>(null) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        border = when {
            isEditMode && isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isEditMode -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            )

            else -> null
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                bounds = Rect(
                    b.left.roundToInt(),
                    b.top.roundToInt(),
                    b.right.roundToInt(),
                    b.bottom.roundToInt(),
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(bounds) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        ) {
            NotificationBadgeBox(apps = listOf(app.key), iconSize = 38.dp) {
                ShapedAppIcon(
                    iconAsset = iconAsset,
                    fallbackGlyph = InvokeIcons.Apps,
                    iconStyle = iconStyle,
                    shape = iconShape,
                    size = 38.dp,
                    monochromeBg = contentColor.copy(alpha = 0.14f),
                    monochromeFg = contentColor,
                )
            }
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmallEmphasized,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun FolderAppsPickerSheet(
    folder: CanvasBlock.FolderTile,
    installedApps: List<InstalledApp>,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onSaveFolder: (String, List<AppKey>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val haptic = LocalHapticFeedback.current
    var draftTitle by remember(folder.id, folder.title) { mutableStateOf(folder.title) }
    var query by remember { mutableStateOf("") }
    var selectedKeys by remember(folder.id, folder.apps) {
        mutableStateOf(folder.apps.toList())
    }

    val filteredApps = remember(installedApps, query) {
        val q = query.trim()
        if (q.isEmpty()) installedApps else installedApps.filter {
            it.label.contains(
                q,
                ignoreCase = true
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.84f)
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit Folder Apps",
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${selectedKeys.size} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSaveFolder(draftTitle.trim().ifBlank { "Folder" }, selectedKeys)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(percent = 50),
                    ) {
                        Text("Save")
                    }
                }

                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Folder name…",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = InvokeIcons.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(percent = 50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Filter apps…",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = InvokeIcons.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { query = "" },
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Icon(
                                    imageVector = InvokeIcons.Close,
                                    contentDescription = "Clear filter",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(percent = 50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    itemsIndexed(
                        items = filteredApps,
                        key = { _, app -> "${app.key.packageName}/${app.key.className}/${app.key.userSerial}" },
                    ) { index, app ->
                        val isChecked = app.key in selectedKeys
                        val iconAsset by produceState<AppIconAsset?>(null, app, appCatalog) {
                            value = appCatalog?.loadIcon(app)
                        }
                        SegmentedListItem(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedKeys = if (isChecked) {
                                    selectedKeys - app.key
                                } else {
                                    selectedKeys + app.key
                                }
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = filteredApps.size
                            ),
                            colors = ListItemDefaults.segmentedColors(
                                containerColor = if (isChecked) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            ),
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(folder.shape.restPolygon().toShape())
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    TileIconGlyph(
                                        iconAsset = iconAsset,
                                        fallbackGlyph = InvokeIcons.Apps,
                                        iconStyle = iconStyle,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            trailingContent = {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                )
                            },
                            content = {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun folderPreviewApp(label: String, pkg: String): InstalledApp = InstalledApp(
    key = AppKey(pkg, "$pkg.MainActivity", 0L),
    label = label,
    componentName = ComponentName(pkg, "$pkg.MainActivity"),
    userHandle = Process.myUserHandle(),
    profile = AppProfile.PERSONAL,
)

@Preview(name = "Folder Bottom Sheet · Bento", showBackground = true, backgroundColor = 0xFF0A0A0C)
@Composable
private fun FolderBottomSheetPreview() {
    val apps = remember {
        listOf(
            folderPreviewApp("Calendar", "org.sample.calendar"),
            folderPreviewApp("Camera", "org.sample.camera"),
            folderPreviewApp("Messages", "org.sample.messages"),
            folderPreviewApp("Clock", "org.sample.clock"),
            folderPreviewApp("Photos", "org.sample.photos"),
            folderPreviewApp("Notes", "org.sample.notes"),
            folderPreviewApp("Music", "org.sample.music"),
        )
    }
    val appsByKey = remember(apps) { apps.associateBy { it.key } }
    val folder = remember(apps) {
        CanvasBlock.FolderTile(
            id = "fld_preview",
            title = "Everyday",
            apps = apps.map { it.key },
            shape = TileShapePreset.COOKIE,
            tone = TileTone.PRIMARY,
        )
    }

    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(),
        ) {
            FolderSheetContent(
                folder = folder,
                appsByKey = appsByKey,
                iconStyle = IconStyle.THEMED_MONOCHROME,
                showLabels = true,
                appCatalog = null,
                actions = FolderSheetActions(),
                onLaunchApp = { _, _ -> },
                onLongPressApp = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            )
        }
    }
}

@Preview(
    name = "Folder Bottom Sheet · Grid Edit",
    showBackground = true,
    backgroundColor = 0xFF0A0A0C
)
@Composable
private fun FolderBottomSheetGridEditPreview() {
    val apps = remember {
        listOf(
            folderPreviewApp("Calendar", "org.sample.calendar"),
            folderPreviewApp("Camera", "org.sample.camera"),
            folderPreviewApp("Messages", "org.sample.messages"),
            folderPreviewApp("Clock", "org.sample.clock"),
        )
    }
    val appsByKey = remember(apps) { apps.associateBy { it.key } }
    val folder = remember(apps) {
        CanvasBlock.FolderTile(
            id = "fld_preview_grid",
            title = "Thumb Zone",
            apps = apps.map { it.key },
            shape = TileShapePreset.SQUIRCLE,
            tone = TileTone.PRIMARY,
            layoutMode = FolderLayoutMode.GRID,
            gridColumns = 3,
            items = listOf(
                FolderItem.SpacerItem(
                    id = "spc_top",
                    span = TileSpan.WIDE_3X1,
                    customColumns = 3,
                    customHeightDp = 72
                ),
                FolderItem.AppItem(
                    id = "i1",
                    appKey = apps[0].key,
                    span = TileSpan.WIDE,
                    tone = TileTone.PRIMARY
                ),
                FolderItem.AppItem(id = "i2", appKey = apps[1].key, span = TileSpan.COMPACT),
                FolderItem.AppItem(id = "i3", appKey = apps[2].key, span = TileSpan.COMPACT),
                FolderItem.AppItem(
                    id = "i4",
                    appKey = apps[3].key,
                    span = TileSpan.WIDE,
                    tone = TileTone.SECONDARY
                ),
            ),
        )
    }

    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(),
        ) {
            FolderSheetContent(
                folder = folder,
                appsByKey = appsByKey,
                iconStyle = IconStyle.THEMED_MONOCHROME,
                showLabels = true,
                appCatalog = null,
                actions = FolderSheetActions(),
                onLaunchApp = { _, _ -> },
                onLongPressApp = {},
                initialEditMode = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            )
        }
    }
}

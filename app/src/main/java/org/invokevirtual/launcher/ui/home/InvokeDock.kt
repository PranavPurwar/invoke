package org.invokevirtual.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.invokevirtual.launcher.R
import org.invokevirtual.launcher.data.DockStyle
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.data.WidgetHeightTier
import org.invokevirtual.launcher.data.WorkspaceMode
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.ui.components.InteractiveColorPicker
import org.invokevirtual.launcher.ui.components.NotificationBadgeBox
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.toContainerShape
import kotlin.math.roundToInt

@Immutable
data class DockQuickAction(
    val id: String,
    val label: String,
    val fallbackIcon: ImageVector,
    val app: InstalledApp? = null,
    val onClick: () -> Unit,
    val onLongClick: () -> Unit = {},
)

@Composable
fun InvokeBottomDock(
    quickActions: List<DockQuickAction>,
    dockStyle: DockStyle,
    showModeButton: Boolean,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    activeMode: WorkspaceMode,
    modeMenuExpanded: Boolean,
    onModeMenuToggle: (Boolean) -> Unit,
    onSelectMode: (WorkspaceMode) -> Unit,
    onEnterEditMode: () -> Unit,
    onAddWidgetClick: () -> Unit,
    onOpenStudio: () -> Unit,
    onInvokeSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchCd = stringResource(R.string.cd_search_invoke)
    val modesCd = stringResource(R.string.cd_workspace_modes)
    val closeCd = stringResource(R.string.cd_close_menu)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (dockStyle == DockStyle.MINIMAL_SEARCH && !showModeButton) {
            Alignment.BottomCenter
        } else {
            Alignment.BottomStart
        },
    ) {
        when (dockStyle) {
            DockStyle.TOOLBAR -> {
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier
                        .align(if (showModeButton) Alignment.BottomStart else Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        toolbarContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    quickActions.forEach { action ->
                        DockActionSlotButton(
                            action = action,
                            iconStyle = iconStyle,
                            appCatalog = appCatalog,
                        )
                    }

                    if (quickActions.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                    }

                    InvokeSearchPillButton(
                        searchCd = searchCd,
                        onClick = onInvokeSearchClick,
                    )
                }
            }

            DockStyle.MINIMAL_SEARCH -> {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .align(if (showModeButton) Alignment.BottomStart else Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                ) {
                    Row(modifier = Modifier.padding(6.dp)) {
                        InvokeSearchPillButton(
                            searchCd = searchCd,
                            onClick = onInvokeSearchClick,
                        )
                    }
                }
            }
        }

        if (showModeButton) {
            FloatingActionButtonMenu(
                expanded = modeMenuExpanded,
                modifier = Modifier.align(Alignment.BottomEnd),
                button = {
                    ToggleFloatingActionButton(
                        checked = modeMenuExpanded,
                        onCheckedChange = onModeMenuToggle,
                        containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                            initialColor = MaterialTheme.colorScheme.tertiaryContainer,
                            finalColor = MaterialTheme.colorScheme.tertiary,
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = if (modeMenuExpanded) closeCd else modesCd
                        },
                    ) {
                        val icon = if (checkedProgress > 0.5f) {
                            InvokeIcons.Close
                        } else {
                            when (activeMode) {
                                WorkspaceMode.EVERYDAY -> InvokeIcons.Spark
                                WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                            }
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (checkedProgress > 0.5f) {
                                MaterialTheme.colorScheme.onTertiary
                            } else {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        onModeMenuToggle(false)
                        onEnterEditMode()
                    },
                    text = {
                        Text(
                            text = "Edit Home",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = InvokeIcons.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        onModeMenuToggle(false)
                        onAddWidgetClick()
                    },
                    text = {
                        Text(
                            text = "Add Widget",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = InvokeIcons.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                FloatingActionButtonMenuItem(
                    onClick = {
                        onModeMenuToggle(false)
                        onOpenStudio()
                    },
                    text = {
                        Text(
                            text = "Studio",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = InvokeIcons.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                WorkspaceMode.entries.forEach { mode ->
                    val isSelected = mode == activeMode
                    FloatingActionButtonMenuItem(
                        onClick = {
                            onSelectMode(mode)
                            onModeMenuToggle(false)
                        },
                        text = {
                            Text(
                                text = mode.label,
                                style = if (isSelected) {
                                    MaterialTheme.typography.titleMediumEmphasized
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (mode) {
                                    WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                                    WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                                    WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DockActionSlotButton(
    action: DockQuickAction,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
) {
    val haptic = LocalHapticFeedback.current
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, action.app, appCatalog) {
        value = if (action.app != null && appCatalog != null) {
            appCatalog.loadIcon(action.app)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(50.dp)
            .clip(CircleShape)
            .semantics { contentDescription = action.label }
            .combinedClickable(
                onClick = action.onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    action.onLongClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        NotificationBadgeBox(apps = listOfNotNull(action.app?.key), iconSize = 38.dp) {
            when {
                iconAsset != null && (iconStyle == IconStyle.FULL_COLOR || iconAsset!!.monochrome == null) -> {
                    Image(
                        bitmap = iconAsset!!.fullColor,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape),
                    )
                }
                iconAsset?.monochrome != null -> {
                    Image(
                        bitmap = iconAsset!!.monochrome!!,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.size(28.dp),
                    )
                }
                else -> {
                    Icon(
                        imageVector = action.fallbackIcon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InvokeSearchPillButton(
    searchCd: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .height(48.dp)
            .semantics { contentDescription = searchCd },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.invoke_search_hint),
                style = MaterialTheme.typography.labelLargeEmphasized,
            )
        }
    }
}

@Composable
fun CanvasEditDock(
    onAddWidgetClick: () -> Unit,
    onAddAppClick: () -> Unit,
    onOpenStudio: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        ButtonGroup(
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            customItem(
                buttonGroupContent = {
                    val interaction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = onAddWidgetClick,
                        interactionSource = interaction,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .animateWidth(interaction),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Widget",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1,
                        )
                    }
                },
                menuContent = { state ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Add Widget") },
                        onClick = {
                            state.dismiss()
                            onAddWidgetClick()
                        },
                    )
                },
            )

            customItem(
                buttonGroupContent = {
                    val interaction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = onAddAppClick,
                        interactionSource = interaction,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .animateWidth(interaction),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1,
                        )
                    }
                },
                menuContent = { state ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Add App or Folder") },
                        onClick = {
                            state.dismiss()
                            onAddAppClick()
                        },
                    )
                },
            )

            customItem(
                buttonGroupContent = {
                    val interaction = remember { MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = onOpenStudio,
                        interactionSource = interaction,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .animateWidth(interaction),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Style",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1,
                        )
                    }
                },
                menuContent = { state ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Studio") },
                        onClick = {
                            state.dismiss()
                            onOpenStudio()
                        },
                    )
                },
            )

            customItem(
                buttonGroupContent = {
                    val interaction = remember { MutableInteractionSource() }
                    Button(
                        onClick = onDone,
                        interactionSource = interaction,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .animateWidth(interaction),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            maxLines = 1,
                        )
                    }
                },
                menuContent = { state ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Done") },
                        onClick = {
                            state.dismiss()
                            onDone()
                        },
                    )
                },
            )
        }
    }
}

private enum class InspectorPanel {
    NONE,
    SIZE,
    SHAPE,
    COLOR,
}

/**
 * Edit Mode Inspector with direct interactive pickers for Size, Shape (for icons, tiles, AND
 * widgets after adding), and Background Color (theme tones, swatches, HSL sliders & EyeDropper).
 */
@Composable
fun CanvasBlockInspectorBar(
    blockId: String,
    title: String,
    sizeLabel: String,
    currentShape: TileShapePreset,
    currentTileSpan: TileSpan?,
    currentWidgetSize: WidgetHeightTier?,
    currentWidgetColumns: Int = 4,
    currentWidgetHeightDp: Int = 196,
    hasCustomWidgetSize: Boolean = false,
    currentTone: TileTone?,
    currentCustomBgArgb: Int?,
    canMoveBack: Boolean,
    canMoveForward: Boolean,
    onMoveBack: () -> Unit,
    onMoveForward: () -> Unit,
    onSelectTileSpan: (TileSpan) -> Unit,
    onSelectWidgetSize: (WidgetHeightTier) -> Unit,
    onPreviewWidgetCustomSize: (Int, Int) -> Unit = { _, _ -> },
    onSelectWidgetCustomSize: (Int, Int) -> Unit = { _, _ -> },
    onSelectShape: (TileShapePreset) -> Unit,
    onSelectTone: (TileTone) -> Unit,
    onSelectCustomBgArgb: (Int?) -> Unit,
    onEditWidget: (() -> Unit)? = null,
    onEditFolder: (() -> Unit)? = null,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    supportsShape: Boolean = true,
    maxColumns: Int = 4,
    minCustomTileHeightDp: Float = 64f,
    maxCustomTileHeightDp: Float = 280f,
) {
    val haptic = LocalHapticFeedback.current
    var activePanel by remember(blockId) { mutableStateOf(InspectorPanel.NONE) }
    var customSizeExpanded by remember(blockId) { mutableStateOf(hasCustomWidgetSize) }
    var draftHeightDp by remember(blockId) {
        mutableFloatStateOf(currentWidgetHeightDp.toFloat())
    }
    LaunchedEffect(blockId, currentWidgetHeightDp) {
        if (kotlin.math.abs(draftHeightDp - currentWidgetHeightDp) > 1.5f) {
            draftHeightDp = currentWidgetHeightDp.toFloat()
        }
    }
    val supportsColor = currentTone != null
    val clampedMaxCols = maxColumns.coerceIn(1, 4)
    val availableTileSpans = remember(clampedMaxCols) {
        TileSpan.entries.filter { it.columns <= clampedMaxCols }
    }

    Column(
        modifier = modifier
            .widthIn(max = 540.dp)
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AnimatedVisibility(visible = activePanel != InspectorPanel.NONE) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                when (activePanel) {
                    InspectorPanel.SIZE -> {
                        if (currentTileSpan != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(availableTileSpans, key = { it.name }) { span ->
                                        FilterChip(
                                            selected = !hasCustomWidgetSize && !customSizeExpanded && span == currentTileSpan,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                customSizeExpanded = false
                                                onSelectTileSpan(span)
                                            },
                                            label = { Text(span.label) },
                                            modifier = Modifier.heightIn(min = 40.dp),
                                        )
                                    }
                                    item(key = "tile_custom_size") {
                                        FilterChip(
                                            selected = hasCustomWidgetSize || customSizeExpanded,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                customSizeExpanded = !customSizeExpanded
                                            },
                                            label = { Text("Custom") },
                                            modifier = Modifier.heightIn(min = 40.dp),
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = customSizeExpanded || hasCustomWidgetSize) {
                                    val minH = minCustomTileHeightDp.roundToInt()
                                    val maxH = maxCustomTileHeightDp.roundToInt()
                                    CustomSizeSliders(
                                        columns = currentWidgetColumns.coerceIn(1, clampedMaxCols),
                                        maxColumns = clampedMaxCols,
                                        draftHeightDp = draftHeightDp,
                                        minHeightDp = minCustomTileHeightDp,
                                        maxHeightDp = maxCustomTileHeightDp,
                                        onSelectColumns = { cols ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectWidgetCustomSize(
                                                cols,
                                                draftHeightDp.roundToInt().coerceIn(minH, maxH),
                                            )
                                        },
                                        onHeightDrag = { next ->
                                            draftHeightDp = next
                                            onPreviewWidgetCustomSize(
                                                currentWidgetColumns.coerceIn(1, clampedMaxCols),
                                                next.roundToInt(),
                                            )
                                        },
                                        onHeightDragFinished = {
                                            onSelectWidgetCustomSize(
                                                currentWidgetColumns.coerceIn(1, clampedMaxCols),
                                                draftHeightDp.roundToInt().coerceIn(minH, maxH),
                                            )
                                        },
                                    )
                                }
                            }
                        } else if (currentWidgetSize != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(WidgetHeightTier.entries, key = { it.name }) { tier ->
                                        FilterChip(
                                            selected = !hasCustomWidgetSize && !customSizeExpanded && tier == currentWidgetSize,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                customSizeExpanded = false
                                                onSelectWidgetSize(tier)
                                            },
                                            label = { Text(tier.label) },
                                            modifier = Modifier.heightIn(min = 40.dp),
                                        )
                                    }
                                    item(key = "widget_custom_size") {
                                        FilterChip(
                                            selected = hasCustomWidgetSize || customSizeExpanded,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                customSizeExpanded = !customSizeExpanded
                                            },
                                            label = { Text("Custom") },
                                            modifier = Modifier.heightIn(min = 40.dp),
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = customSizeExpanded || hasCustomWidgetSize) {
                                    CustomSizeSliders(
                                        columns = currentWidgetColumns,
                                        draftHeightDp = draftHeightDp,
                                        minHeightDp = 88f,
                                        maxHeightDp = 380f,
                                        onSelectColumns = { cols ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectWidgetCustomSize(
                                                cols,
                                                draftHeightDp.roundToInt().coerceIn(88, 380),
                                            )
                                        },
                                        onHeightDrag = { next ->
                                            draftHeightDp = next
                                            onPreviewWidgetCustomSize(
                                                currentWidgetColumns,
                                                next.roundToInt(),
                                            )
                                        },
                                        onHeightDragFinished = {
                                            onSelectWidgetCustomSize(
                                                currentWidgetColumns,
                                                draftHeightDp.roundToInt().coerceIn(88, 380),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                    InspectorPanel.SHAPE -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(TileShapePreset.entries, key = { it.name }) { preset ->
                                val isChosen = preset == currentShape
                                FilterChip(
                                    selected = isChosen,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectShape(preset)
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(preset.toContainerShape(isHero = true))
                                                .background(
                                                    if (isChosen) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                ),
                                        )
                                    },
                                    shape = preset.toContainerShape(isWide = true),
                                    label = { Text(preset.label) },
                                    modifier = Modifier.heightIn(min = 40.dp),
                                )
                            }
                        }
                    }

                    InspectorPanel.COLOR -> {
                        IconBackgroundColorPicker(
                            currentTone = currentTone ?: TileTone.SURFACE,
                            currentCustomBgArgb = currentCustomBgArgb,
                            onSelectTone = onSelectTone,
                            onSelectCustomBgArgb = onSelectCustomBgArgb,
                        )
                    }

                    InspectorPanel.NONE -> Unit
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(
                    onClick = onMoveBack,
                    enabled = canMoveBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.ArrowUp,
                        contentDescription = "Move $title earlier",
                        modifier = Modifier.size(20.dp),
                    )
                }

                IconButton(
                    onClick = onMoveForward,
                    enabled = canMoveForward,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.ArrowDown,
                        contentDescription = "Move $title later",
                        modifier = Modifier.size(20.dp),
                    )
                }

                FilledTonalButton(
                    onClick = {
                        activePanel = if (activePanel == InspectorPanel.SIZE) {
                            InspectorPanel.NONE
                        } else {
                            InspectorPanel.SIZE
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = sizeLabel,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        maxLines = 1,
                    )
                }

                if (supportsShape) {
                    IconButton(
                        onClick = {
                            activePanel = if (activePanel == InspectorPanel.SHAPE) {
                                InspectorPanel.NONE
                            } else {
                                InspectorPanel.SHAPE
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Shapes,
                            contentDescription = "Choose shape for $title",
                            tint = if (activePanel == InspectorPanel.SHAPE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (supportsColor) {
                    IconButton(
                        onClick = {
                            activePanel = if (activePanel == InspectorPanel.COLOR) {
                                InspectorPanel.NONE
                            } else {
                                InspectorPanel.COLOR
                            }
                        },
                        modifier = Modifier.size(48.dp),
                    ) {
                        if (currentCustomBgArgb != null) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentCustomBgArgb))
                                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape),
                            )
                        } else {
                            Icon(
                                imageVector = InvokeIcons.Colorize,
                                contentDescription = "Pick background color for $title",
                                tint = if (activePanel == InspectorPanel.COLOR) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                if (onEditWidget != null) {
                    IconButton(
                        onClick = onEditWidget,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Edit,
                            contentDescription = "Edit widget settings for $title",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                if (onEditFolder != null) {
                    IconButton(
                        onClick = onEditFolder,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Folder,
                            contentDescription = "Manage apps in $title",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = InvokeIcons.Delete,
                        contentDescription = "Remove $title from Home",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomSizeSliders(
    columns: Int,
    draftHeightDp: Float,
    minHeightDp: Float,
    maxHeightDp: Float,
    onSelectColumns: (Int) -> Unit,
    onHeightDrag: (Float) -> Unit,
    onHeightDragFinished: () -> Unit,
    maxColumns: Int = 4,
) {
    val clampedDraft = draftHeightDp.coerceIn(minHeightDp, maxHeightDp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Width",
                style = MaterialTheme.typography.labelMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            (1..maxColumns.coerceIn(1, 4)).forEach { cols ->
                FilterChip(
                    selected = columns == cols,
                    onClick = { onSelectColumns(cols) },
                    label = { Text("${cols} col") },
                    modifier = Modifier.heightIn(min = 36.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Height",
                style = MaterialTheme.typography.labelMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val heightSliderState = rememberSliderState(
                value = clampedDraft,
                trackRange = minHeightDp..maxHeightDp,
            )
            heightSliderState.value = clampedDraft
            Slider(
                state = heightSliderState,
                onValueChange = { next ->
                    heightSliderState.value = next
                    onHeightDrag(next)
                },
                onValueChangeFinished = onHeightDragFinished,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${clampedDraft.roundToInt()}dp",
                style = MaterialTheme.typography.labelMediumEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun IconBackgroundColorPicker(
    currentTone: TileTone,
    currentCustomBgArgb: Int?,
    onSelectTone: (TileTone) -> Unit,
    onSelectCustomBgArgb: (Int?) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(TileTone.entries, key = { it.name }) { tone ->
                val isSelected = currentCustomBgArgb == null && currentTone == tone
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectTone(tone)
                    },
                    label = { Text(tone.label) },
                    modifier = Modifier.heightIn(min = 38.dp),
                )
            }
        }

        InteractiveColorPicker(
            currentArgb = currentCustomBgArgb,
            onSelectColor = { argb -> onSelectCustomBgArgb(argb) },
            onReset = { onSelectCustomBgArgb(null) },
        )
    }
}

/**
 * Expanded-screen (Tablet / Android 17 Desktop Windowing) vertical floating toolbar rail.
 */
@Composable
fun InvokeVerticalDock(
    quickActions: List<DockQuickAction>,
    activeMode: WorkspaceMode,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onAddWidgetClick: () -> Unit,
    onOpenStudio: () -> Unit,
    onCycleMode: () -> Unit,
    onInvokeSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchCd = stringResource(R.string.cd_search_invoke)
    VerticalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
            toolbarContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Surface(
            onClick = onInvokeSearchClick,
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = searchCd },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = InvokeIcons.Search,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        quickActions.take(1).forEach { action ->
            IconButton(
                onClick = action.onClick,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = action.fallbackIcon,
                    contentDescription = action.label,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        IconButton(
            onClick = onToggleEditMode,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = if (isEditMode) InvokeIcons.Check else InvokeIcons.Edit,
                contentDescription = if (isEditMode) "Done editing home" else "Edit Home Canvas",
                tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }

        IconButton(
            onClick = onAddWidgetClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.Widgets,
                contentDescription = "Add Widget to Home",
                modifier = Modifier.size(22.dp),
            )
        }

        IconButton(
            onClick = onOpenStudio,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.Tune,
                contentDescription = "Customization Studio",
                modifier = Modifier.size(22.dp),
            )
        }

        Surface(
            onClick = onCycleMode,
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (activeMode) {
                        WorkspaceMode.EVERYDAY -> InvokeIcons.Sun
                        WorkspaceMode.DEEP_FOCUS -> InvokeIcons.Focus
                        WorkspaceMode.NIGHT_AMOLED -> InvokeIcons.Night
                    },
                    contentDescription = "Cycle workspace mode: ${activeMode.label}",
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

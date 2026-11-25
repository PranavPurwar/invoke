package org.invokevirtual.launcher.ui.home

import android.graphics.Rect
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.WidgetHostController
import org.invokevirtual.launcher.ui.components.NotificationBadge
import org.invokevirtual.launcher.ui.components.NotificationBadgeBox
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.rememberAnimatedContainerMorphShape
import org.invokevirtual.launcher.ui.theme.toContainerShape
import kotlin.math.roundToInt

@Composable
fun CanvasAppBlockCard(
    block: CanvasBlock.AppTile,
    app: InstalledApp?,
    fallbackLabel: String,
    fallbackGlyph: ImageVector,
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
        value = if (app != null && appCatalog != null) appCatalog.loadIcon(app) else null
    }

    val label = app?.label ?: fallbackLabel
    val customColor = block.customBgArgb?.let(::Color)
    val containerColor = customColor ?: block.tone.containerColor()
    val contentColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.contentColor()
    val isFloating = block.effectiveColumns == 1 &&
        (if (block.hasCustomSize) block.effectiveHeightDp <= 116 && block.customBgArgb == null else block.span == TileSpan.COMPACT)

    CanvasTileScaffold(
        span = block.span,
        effectiveColumns = block.effectiveColumns,
        effectiveHeightDp = block.effectiveHeightDp,
        isSquareSize = block.isSquareSize,
        hasCustomSize = block.hasCustomSize,
        shapePreset = block.shape,
        label = label,
        subLabel = null,
        showLabel = showLabel,
        hasCustomBg = block.customBgArgb != null,
        containerColor = containerColor,
        contentColor = contentColor,
        isEditMode = isEditMode,
        isSelected = isSelected,
        accessibilityLabel = label,
        accessibilityActionLabel = if (app != null) "App actions" else null,
        onClick = onClick,
        onLongClick = onLongClick,
        onSelectInEdit = onSelectInEdit,
        modifier = modifier,
        iconContent = { iconSize, morphShape ->
            val isContainerTile = !isFloating
            NotificationBadgeBox(apps = listOfNotNull(app?.key), iconSize = iconSize) {
                ShapedAppIcon(
                    iconAsset = iconAsset,
                    fallbackGlyph = fallbackGlyph,
                    iconStyle = iconStyle,
                    shape = if (isContainerTile) RoundedCornerShape(percent = 26) else morphShape,
                    size = iconSize,
                    monochromeBg = if (isContainerTile) contentColor.copy(alpha = 0.11f) else containerColor,
                    monochromeFg = contentColor,
                    forceContainerBg = !isContainerTile && block.customBgArgb != null,
                )
            }
        },
    )
}

@Composable
fun CanvasShortcutBlockCard(
    block: CanvasBlock.ShortcutTile,
    showLabel: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    appCatalog: AppCatalog?,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedShortcut by produceState<AppShortcutItem?>(
        initialValue = null,
        block.packageName,
        block.shortcutId,
        block.userSerial,
        appCatalog,
    ) {
        value = appCatalog?.resolveShortcut(block.packageName, block.shortcutId, block.userSerial)
    }

    val label = resolvedShortcut?.label ?: block.label
    val subLabel = resolvedShortcut?.appLabel ?: "Shortcut"
    val customColor = block.customBgArgb?.let(::Color)
    val containerColor = customColor ?: block.tone.containerColor()
    val contentColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.contentColor()
    val badgeBgColor = customColor ?: block.tone.badgeContainerColor()
    val badgeFgColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.badgeContentColor()
    val isFloating = block.effectiveColumns == 1 &&
        (if (block.hasCustomSize) block.effectiveHeightDp <= 116 && block.customBgArgb == null else block.span == TileSpan.COMPACT)

    CanvasTileScaffold(
        span = block.span,
        effectiveColumns = block.effectiveColumns,
        effectiveHeightDp = block.effectiveHeightDp,
        isSquareSize = block.isSquareSize,
        hasCustomSize = block.hasCustomSize,
        shapePreset = block.shape,
        label = label,
        subLabel = subLabel,
        showLabel = showLabel,
        hasCustomBg = block.customBgArgb != null,
        containerColor = containerColor,
        contentColor = contentColor,
        isEditMode = isEditMode,
        isSelected = isSelected,
        accessibilityLabel = "$label shortcut",
        accessibilityActionLabel = "Edit shortcut",
        onClick = onClick,
        onLongClick = onLongClick,
        onSelectInEdit = onSelectInEdit,
        modifier = modifier,
        iconContent = { iconSize, morphShape ->
            val isContainerTile = !isFloating
            ShapedShortcutIcon(
                bitmap = resolvedShortcut?.icon,
                shape = if (isContainerTile) RoundedCornerShape(percent = 26) else morphShape,
                size = iconSize,
                bgColor = if (isContainerTile) contentColor.copy(alpha = 0.11f) else containerColor,
                fgColor = contentColor,
                badgeBg = if (isContainerTile) containerColor else badgeBgColor,
                badgeFg = if (isContainerTile) contentColor else badgeFgColor,
            )
        },
    )
}

@Composable
fun CanvasContactBlockCard(
    block: CanvasBlock.ContactTile,
    showLabel: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val customColor = block.customBgArgb?.let(::Color)
    val containerColor = customColor ?: block.tone.containerColor()
    val contentColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.contentColor()
    val badgeBgColor = customColor ?: block.tone.badgeContainerColor()
    val badgeFgColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.badgeContentColor()
    val isFloating = block.effectiveColumns == 1 &&
        (if (block.hasCustomSize) block.effectiveHeightDp <= 116 && block.customBgArgb == null else block.span == TileSpan.COMPACT)

    CanvasTileScaffold(
        span = block.span,
        effectiveColumns = block.effectiveColumns,
        effectiveHeightDp = block.effectiveHeightDp,
        isSquareSize = block.isSquareSize,
        hasCustomSize = block.hasCustomSize,
        shapePreset = block.shape,
        label = block.displayName,
        subLabel = block.phoneNumber,
        showLabel = showLabel,
        hasCustomBg = block.customBgArgb != null,
        containerColor = containerColor,
        contentColor = contentColor,
        isEditMode = isEditMode,
        isSelected = isSelected,
        accessibilityLabel = "${block.displayName}, quick contact",
        accessibilityActionLabel = "Contact options",
        onClick = { onClick() },
        onLongClick = onLongClick,
        onSelectInEdit = onSelectInEdit,
        modifier = modifier,
        iconContent = { iconSize, morphShape ->
            val isContainerTile = !isFloating
            ShapedContactIcon(
                initials = block.initials,
                shape = if (isContainerTile) RoundedCornerShape(percent = 26) else morphShape,
                size = iconSize,
                bgColor = if (isContainerTile) contentColor.copy(alpha = 0.11f) else containerColor,
                fgColor = contentColor,
                badgeBg = if (isContainerTile) containerColor else badgeBgColor,
                badgeFg = if (isContainerTile) contentColor else badgeFgColor,
            )
        },
    )
}

@Composable
fun CanvasSpacerBlockCard(
    block: CanvasBlock.SpacerBlock,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CanvasSpacerPlaceholderCard(
        sizeLabel = block.sizeLabel,
        isSquareSize = block.isSquareSize,
        effectiveHeightDp = block.effectiveHeightDp,
        isEditMode = isEditMode,
        isSelected = isSelected,
        onSelectInEdit = onSelectInEdit,
        modifier = modifier,
    )
}

@Composable
internal fun CanvasSpacerPlaceholderCard(
    sizeLabel: String,
    isSquareSize: Boolean,
    effectiveHeightDp: Int,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val defaultDpSpatial = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val targetHeight = if (isSquareSize) maxWidth else effectiveHeightDp.dp
        val animatedHeight by animateDpAsState(
            targetValue = targetHeight,
            animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
            label = "spacerHeightMorph",
        )
        if (!isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .semantics { contentDescription = "Empty space" },
            )
        } else {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectInEdit()
                },
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.42f)
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .semantics {
                        contentDescription = "Empty space, $sizeLabel"
                        selected = isSelected
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (effectiveHeightDp >= 48) {
                            "Empty Space · $sizeLabel"
                        } else {
                            sizeLabel
                        },
                        style = MaterialTheme.typography.labelSmallEmphasized,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun CanvasFolderBlockCard(
    block: CanvasBlock.FolderTile,
    folderApps: List<InstalledApp>,
    iconStyle: IconStyle,
    showLabel: Boolean,
    isEditMode: Boolean,
    isSelected: Boolean,
    appCatalog: AppCatalog?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val customColor = block.customBgArgb?.let(::Color)
    val containerColor = customColor ?: block.tone.containerColor()
    val contentColor = customColor?.let(::contrastingForegroundFor) ?: block.tone.contentColor()
    val isFloating = block.effectiveColumns == 1 &&
            (if (block.hasCustomSize) block.effectiveHeightDp <= 116 && block.customBgArgb == null else block.span == TileSpan.COMPACT)
    val title = block.title.ifBlank { "Folder" }
    val countLabel = when (folderApps.size) {
        0 -> "Empty"
        1 -> "1 app"
        else -> "${folderApps.size} apps"
    }

    val useBentoTile = !isFloating &&
            block.effectiveColumns >= 2 &&
            (block.isSquareSize || block.effectiveHeightDp >= 136) &&
            folderApps.isNotEmpty()

    if (useBentoTile) {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val fastSpatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
        val defaultDpSpatial = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
        val pressScale by animateFloatAsState(
            targetValue = when {
                isSelected -> 1.03f
                pressed && !isEditMode -> 0.94f
                else -> 1f
            },
            animationSpec = if (reducedMotion) snap() else fastSpatial,
            label = "bentoFolderPressScale",
        )

        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .semantics {
                    contentDescription = "$title folder, $countLabel"
                    if (isEditMode) {
                        selected = isSelected
                    } else {
                        customActions = listOf(
                            CustomAccessibilityAction("Edit folder") {
                                onLongClick()
                                true
                            },
                        )
                    }
                },
        ) {
            val targetHeight = if (block.isSquareSize) maxWidth else block.effectiveHeightDp.dp
            val animatedHeight by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
                label = "bentoFolderHeightMorph",
            )
            val targetAspect = (maxWidth / targetHeight.coerceAtLeast(1.dp)).coerceIn(0.25f, 4.5f)
            val containerShape = rememberAnimatedContainerMorphShape(
                shapePreset = block.shape,
                aspectRatio = targetAspect,
                pressed = pressed && !isEditMode,
                reducedMotion = reducedMotion,
            )
            val itemMorphShape = rememberAnimatedContainerMorphShape(
                shapePreset = block.shape,
                aspectRatio = 1f,
                pressed = pressed && !isEditMode,
                reducedMotion = reducedMotion,
            )

            Surface(
                shape = containerShape,
                color = containerColor,
                contentColor = contentColor,
                border = containerEditBorder(isEditMode, isSelected),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .clip(containerShape)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (isEditMode) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectInEdit()
                            } else {
                                onClick()
                            }
                        },
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.76f),
                            maxLines = 1,
                        )
                        NotificationBadge(
                            apps = folderApps.map { it.key },
                            iconSize = 48.dp,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }

                    FolderBentoMosaic(
                        apps = folderApps,
                        iconStyle = iconStyle,
                        itemShape = itemMorphShape,
                        cellBg = contentColor.copy(alpha = 0.11f),
                        cellFg = contentColor,
                        appCatalog = appCatalog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }
    } else {
        CanvasTileScaffold(
            span = block.span,
            effectiveColumns = block.effectiveColumns,
            effectiveHeightDp = block.effectiveHeightDp,
            isSquareSize = block.isSquareSize,
            hasCustomSize = block.hasCustomSize,
            shapePreset = block.shape,
            label = title,
            subLabel = countLabel,
            showLabel = showLabel,
            hasCustomBg = block.customBgArgb != null,
            containerColor = containerColor,
            contentColor = contentColor,
            isEditMode = isEditMode,
            isSelected = isSelected,
            accessibilityLabel = "$title folder, $countLabel",
            accessibilityActionLabel = "Edit folder",
            onClick = { onClick() },
            onLongClick = onLongClick,
            onSelectInEdit = onSelectInEdit,
            modifier = modifier,
            iconContent = { iconSize, morphShape ->
                val isContainerTile = !isFloating
                NotificationBadgeBox(apps = folderApps.map { it.key }, iconSize = iconSize) {
                    FolderMiniGridIcon(
                        previewApps = folderApps.take(4),
                        iconStyle = iconStyle,
                        shape = morphShape,
                        size = iconSize,
                        bgColor = if (isContainerTile) contentColor.copy(alpha = 0.11f) else containerColor,
                        fgColor = contentColor,
                        appCatalog = appCatalog,
                    )
                }
            },
        )
    }
}

@Composable
private fun FolderBentoMosaic(
    apps: List<InstalledApp>,
    iconStyle: IconStyle,
    itemShape: Shape,
    cellBg: Color,
    cellFg: Color,
    appCatalog: AppCatalog?,
    modifier: Modifier = Modifier,
) {
    when (apps.size) {
        1 -> {
            FolderBentoCell(
                app = apps[0],
                iconStyle = iconStyle,
                itemShape = itemShape,
                cellBg = cellBg,
                cellFg = cellFg,
                iconSize = 46.dp,
                appCatalog = appCatalog,
                modifier = modifier.fillMaxSize(),
            )
        }

        2 -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FolderBentoCell(
                    app = apps[0],
                    iconStyle = iconStyle,
                    itemShape = itemShape,
                    cellBg = cellBg,
                    cellFg = cellFg,
                    iconSize = 40.dp,
                    appCatalog = appCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                FolderBentoCell(
                    app = apps[1],
                    iconStyle = iconStyle,
                    itemShape = itemShape,
                    cellBg = cellBg,
                    cellFg = cellFg,
                    iconSize = 40.dp,
                    appCatalog = appCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        3 -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FolderBentoCell(
                    app = apps[0],
                    iconStyle = iconStyle,
                    itemShape = itemShape,
                    cellBg = cellBg,
                    cellFg = cellFg,
                    iconSize = 44.dp,
                    appCatalog = appCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FolderBentoCell(
                        app = apps[1],
                        iconStyle = iconStyle,
                        itemShape = itemShape,
                        cellBg = cellBg,
                        cellFg = cellFg,
                        iconSize = 26.dp,
                        appCatalog = appCatalog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    FolderBentoCell(
                        app = apps[2],
                        iconStyle = iconStyle,
                        itemShape = itemShape,
                        cellBg = cellBg,
                        cellFg = cellFg,
                        iconSize = 26.dp,
                        appCatalog = appCatalog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            }
        }

        else -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FolderBentoCell(
                    app = apps[0],
                    iconStyle = iconStyle,
                    itemShape = itemShape,
                    cellBg = cellBg,
                    cellFg = cellFg,
                    iconSize = 44.dp,
                    appCatalog = appCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FolderBentoCell(
                        app = apps[1],
                        iconStyle = iconStyle,
                        itemShape = itemShape,
                        cellBg = cellBg,
                        cellFg = cellFg,
                        iconSize = 26.dp,
                        appCatalog = appCatalog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FolderBentoCell(
                            app = apps[2],
                            iconStyle = iconStyle,
                            itemShape = itemShape,
                            cellBg = cellBg,
                            cellFg = cellFg,
                            iconSize = 22.dp,
                            appCatalog = appCatalog,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        FolderBentoCell(
                            app = apps[3],
                            iconStyle = iconStyle,
                            itemShape = itemShape,
                            cellBg = cellBg,
                            cellFg = cellFg,
                            iconSize = 22.dp,
                            appCatalog = appCatalog,
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

@Composable
private fun FolderBentoCell(
    app: InstalledApp,
    iconStyle: IconStyle,
    itemShape: Shape,
    cellBg: Color,
    cellFg: Color,
    iconSize: Dp,
    appCatalog: AppCatalog?,
    modifier: Modifier = Modifier,
) {
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cellBg),
        contentAlignment = Alignment.Center,
    ) {
        ShapedAppIcon(
            iconAsset = iconAsset,
            fallbackGlyph = InvokeIcons.Apps,
            iconStyle = iconStyle,
            shape = itemShape,
            size = iconSize,
            monochromeBg = cellFg.copy(alpha = 0.12f),
            monochromeFg = cellFg,
        )
    }
}

@Composable
internal fun CanvasTileScaffold(
    span: TileSpan,
    effectiveColumns: Int,
    effectiveHeightDp: Int,
    isSquareSize: Boolean,
    hasCustomSize: Boolean,
    shapePreset: org.invokevirtual.launcher.data.TileShapePreset,
    label: String,
    subLabel: String?,
    showLabel: Boolean,
    hasCustomBg: Boolean,
    containerColor: Color,
    contentColor: Color,
    isEditMode: Boolean,
    isSelected: Boolean,
    accessibilityLabel: String,
    accessibilityActionLabel: String?,
    onClick: (Rect?) -> Unit,
    onLongClick: () -> Unit,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable (iconSize: Dp, morphShape: Shape) -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val fastSpatial = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val defaultDpSpatial = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()

    val pressScale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.03f
            pressed && !isEditMode -> 0.94f
            else -> 1f
        },
        animationSpec = if (reducedMotion) snap() else fastSpatial,
        label = "tilePressScale",
    )

    var bounds by remember { mutableStateOf<Rect?>(null) }

    val baseModifier = modifier
        .fillMaxWidth()
        .onGloballyPositioned { coords ->
            val b = coords.boundsInWindow()
            bounds = Rect(
                b.left.roundToInt(),
                b.top.roundToInt(),
                b.right.roundToInt(),
                b.bottom.roundToInt(),
            )
        }
        .graphicsLayer {
            scaleX = pressScale
            scaleY = pressScale
        }
        .semantics {
            contentDescription = accessibilityLabel
            if (isEditMode) {
                selected = isSelected
            } else if (accessibilityActionLabel != null) {
                customActions = listOf(
                    CustomAccessibilityAction(accessibilityActionLabel) {
                        onLongClick()
                        true
                    },
                )
            }
        }

    val clickHandler = {
        if (isEditMode) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSelectInEdit()
        } else {
            onClick(bounds)
        }
    }
    val longClickHandler = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongClick()
    }

    val isFloatingIcon = effectiveColumns == 1 &&
        (if (hasCustomSize) effectiveHeightDp <= 116 && !hasCustomBg else span == TileSpan.COMPACT)

    BoxWithConstraints(modifier = baseModifier) {
        if (isFloatingIcon) {
            val targetIconSize = if (hasCustomSize) {
                (effectiveHeightDp * 0.72f).coerceIn(40f, 88f).dp
            } else {
                60.dp
            }
            val animatedIconSize by animateDpAsState(
                targetValue = targetIconSize,
                animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
                label = "floatingIconSize",
            )
            val floatingMorphShape = rememberAnimatedContainerMorphShape(
                shapePreset = shapePreset,
                aspectRatio = 1f,
                pressed = pressed && !isEditMode,
                reducedMotion = reducedMotion,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(compactEditHighlightModifier(isEditMode, isSelected, hasCustomBg))
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = clickHandler,
                        onLongClick = longClickHandler,
                    )
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                iconContent(animatedIconSize, floatingMorphShape)
                if (showLabel) {
                    Spacer(Modifier.height(6.dp))
                    FloatingTileLabel(label = label)
                }
            }
        } else {
            val targetHeight = if (isSquareSize) maxWidth else effectiveHeightDp.dp
            val animatedHeight by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
                label = "tileContainerHeightMorph",
            )
            val targetAspect = (maxWidth / targetHeight.coerceAtLeast(1.dp)).coerceIn(0.25f, 4.5f)
            val containerShape = rememberAnimatedContainerMorphShape(
                shapePreset = shapePreset,
                aspectRatio = targetAspect,
                pressed = pressed && !isEditMode,
                reducedMotion = reducedMotion,
            )
            val iconMorphShape = rememberAnimatedContainerMorphShape(
                shapePreset = shapePreset,
                aspectRatio = 1f,
                pressed = pressed && !isEditMode,
                reducedMotion = reducedMotion,
            )
            val isHorizontal = targetAspect >= 1.25f
            val isHero = isSquareSize || (effectiveColumns == 2 && targetAspect in 0.82f..1.22f)
            val targetIconSize = when {
                isHorizontal -> (targetHeight * 0.52f)
                    .coerceAtMost(maxWidth * 0.36f)
                    .coerceIn(36.dp, 108.dp)
                isHero -> if (showLabel || subLabel != null) {
                    (min(maxWidth, targetHeight) * 0.48f).coerceIn(56.dp, 124.dp)
                } else {
                    (min(maxWidth, targetHeight) * 0.58f).coerceIn(68.dp, 140.dp)
                }

                effectiveColumns == 1 -> (min(maxWidth, targetHeight) * 0.54f).coerceIn(
                    42.dp,
                    72.dp
                )

                else -> (min(maxWidth, targetHeight) * 0.46f).coerceIn(44.dp, 104.dp)
            }
            val animatedIconSize by animateDpAsState(
                targetValue = targetIconSize,
                animationSpec = if (reducedMotion) snap() else defaultDpSpatial,
                label = "tileContainerIconSize",
            )

            Surface(
                shape = containerShape,
                color = containerColor,
                contentColor = contentColor,
                border = containerEditBorder(isEditMode, isSelected),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedHeight)
                    .clip(containerShape)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = clickHandler,
                        onLongClick = longClickHandler,
                    ),
            ) {
                if (isHorizontal) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (effectiveColumns >= 4 && targetHeight >= 136.dp) {
                                    24.dp
                                } else if (effectiveColumns >= 4) {
                                    22.dp
                                } else {
                                    18.dp
                                },
                                vertical = if (targetHeight >= 136.dp) 18.dp else 10.dp,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (targetHeight >= 136.dp) 18.dp else 12.dp),
                    ) {
                        iconContent(animatedIconSize, iconMorphShape)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = label,
                                style = if (targetHeight >= 136.dp) {
                                    MaterialTheme.typography.headlineSmallEmphasized
                                } else {
                                    MaterialTheme.typography.titleMediumEmphasized
                                },
                                color = contentColor,
                                maxLines = if (targetHeight >= 136.dp) 2 else 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (subLabel != null) {
                                Text(
                                    text = subLabel,
                                    style = if (targetHeight >= 136.dp) {
                                        MaterialTheme.typography.bodyMedium
                                    } else {
                                        MaterialTheme.typography.labelSmall
                                    },
                                    color = contentColor.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                } else {
                    val isNarrow = effectiveColumns == 1
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (isNarrow) 10.dp else 14.dp,
                                vertical = if (isHero) 16.dp else 18.dp,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = if (isHero || isNarrow || targetHeight < 136.dp) {
                            Arrangement.spacedBy(
                                if (isHero) 10.dp else 8.dp,
                                Alignment.CenterVertically
                            )
                        } else {
                            Arrangement.SpaceBetween
                        },
                    ) {
                        iconContent(animatedIconSize, iconMorphShape)
                        if (showLabel || subLabel != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = label,
                                    style = when {
                                        isNarrow -> MaterialTheme.typography.labelLargeEmphasized
                                        isHero -> MaterialTheme.typography.titleMediumEmphasized
                                        else -> MaterialTheme.typography.titleLargeEmphasized
                                    },
                                    color = contentColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (subLabel != null) {
                                    Text(
                                        text = subLabel,
                                        style = if (isNarrow) {
                                            MaterialTheme.typography.labelSmall
                                        } else {
                                            MaterialTheme.typography.labelMedium
                                        },
                                        color = contentColor.copy(alpha = 0.78f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
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

@Composable
private fun FloatingTileLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMediumEmphasized.copy(
            shadow = Shadow(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f),
                offset = Offset(0f, 1f),
                blurRadius = 1.5f,
            ),
        ),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun compactEditHighlightModifier(
    isEditMode: Boolean,
    isSelected: Boolean,
    hasCustomBg: Boolean,
): Modifier = when {
    isEditMode && isSelected -> Modifier
        .clip(RoundedCornerShape(22.dp))
        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f))
        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp))
    isEditMode -> Modifier
        .clip(RoundedCornerShape(22.dp))
        .border(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            RoundedCornerShape(22.dp),
        )
    hasCustomBg -> Modifier
    else -> Modifier
}

@Composable
private fun containerEditBorder(
    isEditMode: Boolean,
    isSelected: Boolean,
): BorderStroke? = when {
    isEditMode && isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    isEditMode -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    else -> null
}

@Composable
internal fun ShapedAppIcon(
    iconAsset: AppIconAsset?,
    fallbackGlyph: ImageVector,
    iconStyle: IconStyle,
    shape: Shape,
    size: Dp,
    monochromeBg: Color,
    monochromeFg: Color,
    forceContainerBg: Boolean = false,
) {
    val useFullColor = iconAsset != null &&
        (iconStyle == IconStyle.FULL_COLOR || iconAsset.monochrome == null)

    if (useFullColor && !forceContainerBg) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = iconAsset.fullColor,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else if (useFullColor) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(monochromeBg),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = iconAsset.fullColor,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size * 0.76f)
                    .clip(shape),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(monochromeBg),
            contentAlignment = Alignment.Center,
        ) {
            if (iconAsset?.monochrome != null) {
                Image(
                    bitmap = iconAsset.monochrome,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(monochromeFg),
                    modifier = Modifier.size(size * 0.58f),
                )
            } else {
                Icon(
                    imageVector = fallbackGlyph,
                    contentDescription = null,
                    tint = monochromeFg,
                    modifier = Modifier.size(size * 0.52f),
                )
            }
        }
    }
}

@Composable
private fun ShapedShortcutIcon(
    bitmap: ImageBitmap?,
    shape: Shape,
    size: Dp,
    bgColor: Color,
    fgColor: Color,
    badgeBg: Color,
    badgeFg: Color,
) {
    val badgeSize = (size * 0.32f).coerceIn(18.dp, 26.dp)
    val badgeIconSize = (size * 0.17f).coerceIn(10.dp, 14.dp)
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size * 0.76f)
                        .clip(shape),
                )
            } else {
                Icon(
                    imageVector = InvokeIcons.ShortcutBolt,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(size * 0.48f),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeSize)
                .clip(CircleShape)
                .background(badgeBg)
                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = InvokeIcons.ShortcutBolt,
                contentDescription = null,
                tint = badgeFg,
                modifier = Modifier.size(badgeIconSize),
            )
        }
    }
}

@Composable
private fun ShapedContactIcon(
    initials: String,
    shape: Shape,
    size: Dp,
    bgColor: Color,
    fgColor: Color,
    badgeBg: Color,
    badgeFg: Color,
) {
    val badgeSize = (size * 0.32f).coerceIn(18.dp, 26.dp)
    val badgeIconSize = (size * 0.17f).coerceIn(10.dp, 14.dp)
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = if (size >= 56.dp) {
                    MaterialTheme.typography.titleLargeEmphasized
                } else {
                    MaterialTheme.typography.titleMediumEmphasized
                },
                color = fgColor,
                maxLines = 1,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeSize)
                .clip(CircleShape)
                .background(badgeBg)
                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = InvokeIcons.Phone,
                contentDescription = null,
                tint = badgeFg,
                modifier = Modifier.size(badgeIconSize),
            )
        }
    }
}

@Composable
private fun FolderMiniGridIcon(
    previewApps: List<InstalledApp>,
    iconStyle: IconStyle,
    shape: Shape,
    size: Dp,
    bgColor: Color,
    fgColor: Color,
    appCatalog: AppCatalog?,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (previewApps.isEmpty()) {
            Icon(
                imageVector = InvokeIcons.Folder,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(size * 0.48f),
            )
        } else {
            val gridPad = (size * 0.14f).coerceAtLeast(4.dp)
            val cellGap = (size * 0.06f).coerceAtLeast(2.dp)
            val miniSize = ((size - gridPad * 2 - cellGap) / 2f).coerceAtLeast(8.dp)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(gridPad),
                verticalArrangement = Arrangement.spacedBy(cellGap, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (rowIdx in 0..1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            cellGap,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        for (colIdx in 0..1) {
                            val app = previewApps.getOrNull(rowIdx * 2 + colIdx)
                            if (app != null) {
                                val iconAsset by produceState<AppIconAsset?>(
                                    initialValue = null,
                                    app,
                                    appCatalog,
                                ) {
                                    value = appCatalog?.loadIcon(app)
                                }
                                ShapedAppIcon(
                                    iconAsset = iconAsset,
                                    fallbackGlyph = InvokeIcons.Apps,
                                    iconStyle = iconStyle,
                                    shape = shape,
                                    size = miniSize,
                                    monochromeBg = fgColor.copy(alpha = 0.14f),
                                    monochromeFg = fgColor,
                                )
                            } else {
                                Box(modifier = Modifier.size(miniSize))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders a native Android `AppWidgetHostView` directly edge-to-edge on the Home Canvas —
 * supports preset sizes (`2×1`, `2×2` true square, `4×1`, `4×2`, `4×3`) AND custom `1..4 col × dp`
 * widget dimensions with smooth height animation (no polygon morphing or long-click).
 */
@Composable
fun CanvasWidgetBlockCard(
    block: CanvasBlock.WidgetBlock,
    widgetHost: WidgetHostController?,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelectInEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current
    val info = remember(block.appWidgetId, widgetHost) {
        widgetHost?.getWidgetInfo(block.appWidgetId)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val targetHeight = if (block.isSquareSize) maxWidth else block.effectiveHeightDp.dp
        val animatedHeight by animateDpAsState(
            targetValue = targetHeight,
            animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
            label = "widgetHeight",
        )
        val targetWidthDp = maxWidth.value.roundToInt().coerceAtLeast(1)
        val targetHeightDp = targetHeight.value.roundToInt().coerceAtLeast(1)
        val targetAspect = (maxWidth / targetHeight.coerceAtLeast(1.dp)).coerceIn(0.25f, 4.5f)

        val containerShape = block.shape.toContainerShape(
            isWide = block.effectiveColumns >= 2 && targetAspect >= 1.25f,
            isHero = block.isSquareSize || targetAspect in 0.84f..1.18f,
            isTall = targetAspect < 0.84f,
        )

        var lastDispatchedWidthDp by remember { mutableIntStateOf(-1) }
        var lastDispatchedHeightDp by remember { mutableIntStateOf(-1) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
                .clip(containerShape)
                .then(
                    when {
                        isEditMode && isSelected -> Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = containerShape,
                        )
                        isEditMode -> Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            shape = containerShape,
                        )
                        else -> Modifier
                    },
                ),
        ) {
            if (widgetHost != null && info != null) {
                AndroidView(
                    factory = { viewContext ->
                        widgetHost.createHostView(viewContext, block.appWidgetId, info)
                    },
                    update = { hostView ->
                        hostView.setPadding(0, 0, 0, 0)
                        if (targetWidthDp != lastDispatchedWidthDp || targetHeightDp != lastDispatchedHeightDp) {
                            lastDispatchedWidthDp = targetWidthDp
                            lastDispatchedHeightDp = targetHeightDp
                            widgetHost.updateWidgetSize(hostView, targetWidthDp, targetHeightDp)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isEditMode) {
                            "Widget preview · ${block.sizeLabel} · ${block.shape.label}"
                        } else {
                            "Widget unavailable · Edit to remove"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            } else {
                                Color.Transparent
                            },
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectInEdit()
                        },
                )
            }
        }
    }
}

internal fun contrastingForegroundFor(bg: Color): Color =
    if (bg.luminance() > 0.42f) Color(0xFF141218) else Color.White

@Composable
internal fun TileTone.containerColor(): Color = when (this) {
    TileTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    TileTone.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
    TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f)
    TileTone.SURFACE -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f)
}

@Composable
internal fun TileTone.contentColor(): Color = when (this) {
    TileTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    TileTone.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    TileTone.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    TileTone.SURFACE -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun TileTone.badgeContainerColor(): Color = when (this) {
    TileTone.PRIMARY -> MaterialTheme.colorScheme.primary
    TileTone.SECONDARY -> MaterialTheme.colorScheme.secondary
    TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiary
    TileTone.SURFACE -> MaterialTheme.colorScheme.primaryContainer
}

@Composable
private fun TileTone.badgeContentColor(): Color = when (this) {
    TileTone.PRIMARY -> MaterialTheme.colorScheme.onPrimary
    TileTone.SECONDARY -> MaterialTheme.colorScheme.onSecondary
    TileTone.TERTIARY -> MaterialTheme.colorScheme.onTertiary
    TileTone.SURFACE -> MaterialTheme.colorScheme.onPrimaryContainer
}

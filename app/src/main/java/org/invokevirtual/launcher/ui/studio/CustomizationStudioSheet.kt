package org.invokevirtual.launcher.ui.studio

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.invokevirtual.launcher.data.BadgeStyle
import org.invokevirtual.launcher.data.ClockAlignment
import org.invokevirtual.launcher.data.ClockSize
import org.invokevirtual.launcher.data.ClockStyle
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.DockStyle
import org.invokevirtual.launcher.data.ExpressivePalette
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.LauncherFontPreset
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.SettingsStore
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.DefaultLauncherHelper
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.NotificationAccess
import org.invokevirtual.launcher.system.ScreenLock
import org.invokevirtual.launcher.ui.components.InteractiveColorPicker
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeShapes
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.googleFontFamily
import org.invokevirtual.launcher.ui.theme.rememberMorphShape
import org.invokevirtual.launcher.ui.theme.swatchColor
import org.invokevirtual.launcher.ui.theme.toContainerShape

@Immutable
data class StudioActions(
    val onToggleShowClock: (Boolean) -> Unit = {},
    val onSelectClockStyle: (ClockStyle) -> Unit = {},
    val onSelectClockSize: (ClockSize) -> Unit = {},
    val onSelectClockAlignment: (ClockAlignment) -> Unit = {},
    val onSelectClockTone: (TileTone) -> Unit = {},
    val onPickClockCustomColorArgb: (Int?) -> Unit = {},
    val onSelectClockShape: (TileShapePreset) -> Unit = {},
    val onToggleClock24Hour: (Boolean) -> Unit = {},
    val onToggleClockSeconds: (Boolean) -> Unit = {},
    val onToggleClockAmPm: (Boolean) -> Unit = {},
    val onToggleClockDate: (Boolean) -> Unit = {},
    val onToggleClockModeBadge: (Boolean) -> Unit = {},
    val onToggleGlanceBattery: (Boolean) -> Unit = {},
    val onToggleGlanceAlarm: (Boolean) -> Unit = {},
    val onSelectFontPreset: (LauncherFontPreset) -> Unit = {},
    val onSelectDarkMode: (DarkMode) -> Unit = {},
    val onToggleAmoledBlack: (Boolean) -> Unit = {},
    val onToggleDynamicColor: (Boolean) -> Unit = {},
    val onSelectPalette: (ExpressivePalette) -> Unit = {},
    val onPickCustomSeedArgb: (Int?) -> Unit = {},
    val onSelectIconStyle: (IconStyle) -> Unit = {},
    val onToggleShowLabels: (Boolean) -> Unit = {},
    val onToggleExpressiveMotion: (Boolean) -> Unit = {},
    val onToggleShowDock: (Boolean) -> Unit = {},
    val onSelectDockStyle: (DockStyle) -> Unit = {},
    val onToggleShowDockModeButton: (Boolean) -> Unit = {},
    val onSetDockApps: (List<AppKey>) -> Unit = {},
    val onSetDrawerTransparency: (Float) -> Unit = {},
    val onToggleShowQuickPeople: (Boolean) -> Unit = {},
    val onToggleBottomSearchBar: (Boolean) -> Unit = {},
    val onToggleDoubleTapToSleep: (Boolean) -> Unit = {},
    val onSelectBadgeStyle: (BadgeStyle) -> Unit = {},
    val onRequestSetDefaultLauncher: () -> Unit = {},
)

@Composable
fun CustomizationStudioSheet(
    settings: LauncherSettings,
    actions: StudioActions,
    installedApps: List<InstalledApp> = emptyList(),
    appCatalog: AppCatalog? = null,
    onDismiss: () -> Unit,
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
        CustomizationStudioContent(
            settings = settings,
            actions = actions,
            installedApps = installedApps,
            appCatalog = appCatalog,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
        )
    }
}

@Composable
internal fun CustomizationStudioContent(
    settings: LauncherSettings,
    actions: StudioActions,
    modifier: Modifier = Modifier,
    installedApps: List<InstalledApp> = emptyList(),
    appCatalog: AppCatalog? = null,
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val haptic = LocalHapticFeedback.current
    var showSeedPicker by remember { mutableStateOf(settings.customSeedArgb != null) }
    var showClockColorPicker by remember { mutableStateOf(settings.clockCustomColorArgb != null) }
    var dockPickerSlotIndex by remember { mutableStateOf<Int?>(null) }

    val openWallpaperPicker: () -> Unit = {
        if (!inPreview) {
            runCatching {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SET_WALLPAPER),
                    "Choose wallpaper",
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "studio_header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = InvokeIcons.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Customization Studio",
                                style = MaterialTheme.typography.headlineSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Wallpaper, fonts, clock, drawer & dock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    LiveStudioPreviewBanner(settings = settings)

                    if (!DefaultLauncherHelper.isDefaultLauncher(context)) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    actions.onRequestSetDefaultLauncher()
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = InvokeIcons.Home,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Set as Default Launcher",
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "Tap to make Invoke your home app",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.8f
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Icon(
                                    imageVector = InvokeIcons.OpenInNew,
                                    contentDescription = "Set as default launcher",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }

                    // Prominent Sponsor Callout Card
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://invokevirtual.org/sponsor/"),
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching { context.startActivity(intent) }
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = InvokeIcons.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Support & Sponsor Invoke",
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "100% independent & ad-free",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = InvokeIcons.OpenInNew,
                                contentDescription = "Open donation link",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            item(key = "wallpaper_and_color") {
                StudioSection(title = "Wallpaper & Color") {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        val hasDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        val rowCount = if (hasDynamicColor) 4 else 3
                        var rowIdx = 0

                        Surface(
                            shape = InvokeShapes.segmentedItemShape(rowIdx++, rowCount),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = InvokeIcons.Spark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Wallpaper",
                                        style = MaterialTheme.typography.titleSmallEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Change Home & Lock screen wallpaper",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        openWallpaperPicker()
                                    },
                                    shape = RoundedCornerShape(percent = 50),
                                    modifier = Modifier.heightIn(min = 48.dp),
                                ) {
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelLargeEmphasized,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        if (hasDynamicColor) {
                            StudioSwitchRow(
                                title = "Wallpaper Dynamic Color",
                                subtitle = "Extract Material You tones from wallpaper",
                                checked = settings.dynamicColor,
                                shape = InvokeShapes.segmentedItemShape(rowIdx++, rowCount),
                                onCheckedChange = actions.onToggleDynamicColor,
                            )
                        }

                        Surface(
                            shape = InvokeShapes.segmentedItemShape(rowIdx++, rowCount),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Curated Palettes",
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    ExpressivePalette.entries.forEach { palette ->
                                        val isActive = !settings.dynamicColor &&
                                            settings.customSeedArgb == null &&
                                            settings.palette == palette
                                        PaletteSwatchOption(
                                            label = palette.displayName,
                                            swatchColor = palette.swatchColor(),
                                            selected = isActive,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                actions.onSelectPalette(palette)
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = InvokeShapes.segmentedItemShape(rowIdx, rowCount),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                settings.customSeedArgb?.let(::Color)
                                                    ?: MaterialTheme.colorScheme.tertiaryContainer,
                                            )
                                            .border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = InvokeIcons.Colorize,
                                            contentDescription = null,
                                            tint = if (settings.customSeedArgb != null) {
                                                Color.White
                                            } else {
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            },
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Custom Accent Seed",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = if (settings.customSeedArgb != null) {
                                                "Custom seed active"
                                            } else {
                                                "Pick any accent hue or sample screen"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            showSeedPicker = !showSeedPicker
                                        },
                                        shape = RoundedCornerShape(percent = 50),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        ),
                                        modifier = Modifier.heightIn(min = 48.dp),
                                    ) {
                                        Text(
                                            text = if (showSeedPicker) "Hide" else "Pick",
                                            style = MaterialTheme.typography.labelLargeEmphasized,
                                            maxLines = 1,
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showSeedPicker || settings.customSeedArgb != null) {
                                    InteractiveColorPicker(
                                        currentArgb = settings.customSeedArgb,
                                        onSelectColor = { argb -> actions.onPickCustomSeedArgb(argb) },
                                        onReset = {
                                            actions.onPickCustomSeedArgb(null)
                                            showSeedPicker = false
                                        },
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "clock_header") {
                StudioSection(title = "Clock & Header") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StudioSwitchRow(
                            title = "Show Clock",
                            subtitle = "Toggle the home clock face on or off",
                            checked = settings.showClock,
                            shape = RoundedCornerShape(20.dp),
                            onCheckedChange = actions.onToggleShowClock,
                        )

                        AnimatedVisibility(visible = settings.showClock) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ConnectedToggleSelector(
                                    options = ClockStyle.entries,
                                    selected = settings.clockStyle,
                                    labelOf = { it.label },
                                    onSelect = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        actions.onSelectClockStyle(it)
                                    },
                                )

                                ConnectedToggleSelector(
                                    options = ClockSize.entries,
                                    selected = settings.clockSize,
                                    labelOf = { it.label },
                                    onSelect = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        actions.onSelectClockSize(it)
                                    },
                                )

                                ConnectedToggleSelector(
                                    options = ClockAlignment.entries,
                                    selected = settings.clockAlignment,
                                    labelOf = { it.label },
                                    onSelect = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        actions.onSelectClockAlignment(it)
                                    },
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "Clock Color",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            TileTone.entries.forEach { tone ->
                                                val isSelected =
                                                    settings.clockCustomColorArgb == null && settings.clockTone == tone
                                                val swatch = when (tone) {
                                                    TileTone.PRIMARY -> MaterialTheme.colorScheme.primary
                                                    TileTone.SECONDARY -> MaterialTheme.colorScheme.secondary
                                                    TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiary
                                                    TileTone.SURFACE -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        showClockColorPicker = false
                                                        actions.onSelectClockTone(tone)
                                                    },
                                                    leadingIcon = {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(12.dp)
                                                                .clip(CircleShape)
                                                                .background(swatch),
                                                        )
                                                    },
                                                    label = { Text(tone.label) },
                                                )
                                            }
                                            FilterChip(
                                                selected = settings.clockCustomColorArgb != null || showClockColorPicker,
                                                onClick = { showClockColorPicker = !showClockColorPicker },
                                                leadingIcon = settings.clockCustomColorArgb?.let { argb ->
                                                    {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(12.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(argb)),
                                                        )
                                                    }
                                                },
                                                label = { Text("Color Picker") },
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = showClockColorPicker || settings.clockCustomColorArgb != null,
                                        ) {
                                            InteractiveColorPicker(
                                                currentArgb = settings.clockCustomColorArgb,
                                                onSelectColor = { argb -> actions.onPickClockCustomColorArgb(argb) },
                                                onReset = {
                                                    actions.onPickClockCustomColorArgb(null)
                                                    showClockColorPicker = false
                                                },
                                                modifier = Modifier.padding(top = 4.dp),
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = settings.clockStyle == ClockStyle.SHAPE_DIAL ||
                                        (settings.clockStyle == ClockStyle.EDITORIAL_STACKED && settings.showClockModeBadge),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = if (settings.clockStyle == ClockStyle.SHAPE_DIAL) {
                                                    "Dial Shape"
                                                } else {
                                                    "Clock Badge Shape"
                                                },
                                                style = MaterialTheme.typography.titleSmallEmphasized,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                            )
                                            LazyRow(
                                                contentPadding = PaddingValues(horizontal = 16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                items(TileShapePreset.entries, key = { it.name }) { preset ->
                                                    val isChosen = preset == settings.clockShape
                                                    FilterChip(
                                                        selected = isChosen,
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            actions.onSelectClockShape(preset)
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
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Visible Header Details",
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    if (settings.showClock) {
                                        FilterChip(
                                            selected = settings.clock24Hour,
                                            onClick = { actions.onToggleClock24Hour(!settings.clock24Hour) },
                                            label = { Text("24-Hour") },
                                        )
                                        FilterChip(
                                            selected = settings.showClockSeconds,
                                            onClick = { actions.onToggleClockSeconds(!settings.showClockSeconds) },
                                            label = { Text("Seconds") },
                                        )
                                        if (!settings.clock24Hour) {
                                            FilterChip(
                                                selected = settings.showClockAmPm,
                                                onClick = { actions.onToggleClockAmPm(!settings.showClockAmPm) },
                                                label = { Text("AM/PM") },
                                            )
                                        }
                                        FilterChip(
                                            selected = settings.showClockDate,
                                            onClick = { actions.onToggleClockDate(!settings.showClockDate) },
                                            label = { Text("Date & Day") },
                                        )
                                    }
                                    FilterChip(
                                        selected = settings.showClockModeBadge,
                                        onClick = { actions.onToggleClockModeBadge(!settings.showClockModeBadge) },
                                        label = { Text("Mode Switcher") },
                                    )
                                    FilterChip(
                                        selected = settings.showGlanceBattery,
                                        onClick = { actions.onToggleGlanceBattery(!settings.showGlanceBattery) },
                                        label = { Text("Battery") },
                                    )
                                    FilterChip(
                                        selected = settings.showGlanceAlarm,
                                        onClick = { actions.onToggleGlanceAlarm(!settings.showGlanceAlarm) },
                                        label = { Text("Next Alarm") },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "typography_pairings") {
                StudioSection(title = "Font Pairing") {
                    ConnectedToggleSelector(
                        options = LauncherFontPreset.entries,
                        selected = settings.fontPreset,
                        labelOf = { it.displayName },
                        onSelect = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            actions.onSelectFontPreset(it)
                        },
                    )
                }
            }

            item(key = "dock_customization") {
                val appsByKey = remember(installedApps) { installedApps.associateBy { it.key } }
                StudioSection(title = "Bottom Dock") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StudioSwitchRow(
                            title = "Show Bottom Dock",
                            subtitle = "Swipe up anytime to open Invoke search",
                            checked = settings.showDock,
                            shape = RoundedCornerShape(20.dp),
                            onCheckedChange = actions.onToggleShowDock,
                        )

                        AnimatedVisibility(visible = settings.showDock) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ConnectedToggleSelector(
                                    options = DockStyle.entries,
                                    selected = settings.dockStyle,
                                    labelOf = { it.label },
                                    onSelect = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        actions.onSelectDockStyle(it)
                                    },
                                )

                                StudioSwitchRow(
                                    title = "Mode Action Button",
                                    subtitle = "Floating menu button beside the dock",
                                    checked = settings.showDockModeButton,
                                    shape = RoundedCornerShape(20.dp),
                                    onCheckedChange = actions.onToggleShowDockModeButton,
                                )

                                if (settings.dockStyle == DockStyle.TOOLBAR) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = "Dock Shortcuts (${settings.dockApps.size}/${SettingsStore.MAX_DOCK_APPS})",
                                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                if (settings.dockApps.isNotEmpty()) {
                                                    TextButton(onClick = { actions.onSetDockApps(emptyList()) }) {
                                                        Text("Auto Defaults")
                                                    }
                                                }
                                            }
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                settings.dockApps.forEachIndexed { idx, appKey ->
                                                    val label = appsByKey[appKey]?.label
                                                        ?: appKey.packageName.substringAfterLast('.')
                                                    FilterChip(
                                                        selected = true,
                                                        onClick = { dockPickerSlotIndex = idx },
                                                        trailingIcon = {
                                                            Icon(
                                                                imageVector = InvokeIcons.Close,
                                                                contentDescription = "Remove $label from dock",
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .clickable {
                                                                        val next = settings.dockApps.toMutableList()
                                                                        next.removeAt(idx)
                                                                        actions.onSetDockApps(next)
                                                                    },
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                text = label,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        },
                                                    )
                                                }

                                                if (settings.dockApps.size < SettingsStore.MAX_DOCK_APPS &&
                                                    installedApps.isNotEmpty()
                                                ) {
                                                    FilterChip(
                                                        selected = false,
                                                        onClick = { dockPickerSlotIndex = settings.dockApps.size },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = InvokeIcons.Add,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp),
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                if (settings.dockApps.isEmpty()) "Customize Slots" else "Add App",
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            if (settings.dockApps.isEmpty()) {
                                                Text(
                                                    text = "Currently using smart defaults (Phone, Messages, Camera).",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            item(key = "app_drawer_style") {
                var transparencyDraft by remember(settings.drawerTransparency) {
                    mutableFloatStateOf(settings.drawerTransparency)
                }
                val percent = (transparencyDraft * 100f).toInt().coerceIn(0, 100)
                StudioSection(title = "App Drawer") {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        StudioSwitchRow(
                            title = "Bottom Search Bar",
                            subtitle = "Place search bar at bottom with results growing upward",
                            checked = settings.bottomSearchBar,
                            shape = InvokeShapes.segmentedItemShape(0, 3),
                            onCheckedChange = actions.onToggleBottomSearchBar,
                        )
                        StudioSwitchRow(
                            title = "Quick People",
                            subtitle = "Show 1-tap call & text favorites in the drawer",
                            checked = settings.showQuickPeople,
                            shape = InvokeShapes.segmentedItemShape(1, 3),
                            onCheckedChange = actions.onToggleShowQuickPeople,
                        )
                        Surface(
                            shape = InvokeShapes.segmentedItemShape(2, 3),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Drawer Transparency",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = if (percent == 0) {
                                                "Solid sheet · 0% transparent"
                                            } else {
                                                "Let wallpaper show through · $percent%"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (percent > 0) {
                                        TextButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                transparencyDraft = 0f
                                                actions.onSetDrawerTransparency(0f)
                                            },
                                        ) {
                                            Text("Reset")
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    val sliderState = rememberSliderState(
                                        value = transparencyDraft,
                                        trackRange = 0f..1f,
                                    )
                                    sliderState.value = transparencyDraft
                                    Slider(
                                        state = sliderState,
                                        onValueChange = { next ->
                                            sliderState.value = next
                                            transparencyDraft = next
                                        },
                                        onValueChangeFinished = {
                                            actions.onSetDrawerTransparency(transparencyDraft)
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelLargeEmphasized,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "theme_oled") {
                StudioSection(title = "Theme & OLED") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ConnectedToggleSelector(
                            options = DarkMode.entries,
                            selected = settings.darkMode,
                            labelOf = { it.label },
                            onSelect = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                actions.onSelectDarkMode(it)
                            },
                        )
                        StudioSwitchRow(
                            title = "True-Black OLED",
                            subtitle = "Pure dark canvas & zero tonal elevation",
                            checked = settings.amoledBlack,
                            shape = RoundedCornerShape(20.dp),
                            onCheckedChange = actions.onToggleAmoledBlack,
                        )
                    }
                }
            }

            item(key = "icons_motion") {
                StudioSection(title = "Icons & Motion") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ConnectedToggleSelector(
                            options = IconStyle.entries,
                            selected = settings.iconStyle,
                            labelOf = { it.label },
                            onSelect = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                actions.onSelectIconStyle(it)
                            },
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            StudioSwitchRow(
                                title = "Show App Labels",
                                subtitle = "Display app titles beneath Home Canvas icons",
                                checked = settings.showLabels,
                                shape = InvokeShapes.segmentedItemShape(0, 2),
                                onCheckedChange = actions.onToggleShowLabels,
                            )
                            StudioSwitchRow(
                                title = "Expressive Motion",
                                subtitle = "Spring shape morphs · off enables Calm mode",
                                checked = settings.expressiveMotion,
                                shape = InvokeShapes.segmentedItemShape(1, 2),
                                onCheckedChange = actions.onToggleExpressiveMotion,
                            )
                        }
                        NotificationBadgeControls(
                            style = settings.badgeStyle,
                            onSelect = actions.onSelectBadgeStyle,
                        )
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                item(key = "gestures") {
                    StudioSection(title = "Gestures") {
                        DoubleTapToSleepControls(
                            enabled = settings.doubleTapToSleep,
                            onToggle = actions.onToggleDoubleTapToSleep,
                        )
                    }
                }
            }
        }
    }

    dockPickerSlotIndex?.let { slotIndex ->
        DockAppPickerDialog(
            installedApps = installedApps,
            appCatalog = appCatalog,
            onDismiss = { dockPickerSlotIndex = null },
            onPickApp = { picked ->
                val updated = settings.dockApps.toMutableList()
                if (slotIndex in updated.indices) {
                    updated[slotIndex] = picked.key
                } else {
                    updated.add(picked.key)
                }
                actions.onSetDockApps(updated)
                dockPickerSlotIndex = null
            },
        )
    }
}

@Composable
private fun DockAppPickerDialog(
    installedApps: List<InstalledApp>,
    appCatalog: AppCatalog?,
    onDismiss: () -> Unit,
    onPickApp: (InstalledApp) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Dock App",
                style = MaterialTheme.typography.headlineSmallEmphasized,
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                itemsIndexed(
                    items = installedApps,
                    key = { _, app -> "${app.key.packageName}/${app.key.className}/${app.key.userSerial}" },
                ) { index, app ->
                    val iconAsset by produceState<AppIconAsset?>(null, app, appCatalog) {
                        value = appCatalog?.loadIcon(app)
                    }
                    SegmentedListItem(
                        onClick = { onPickApp(app) },
                        shapes = ListItemDefaults.segmentedShapes(index, installedApps.size),
                        colors = ListItemDefaults.segmentedColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        leadingContent = {
                            if (iconAsset != null) {
                                Image(
                                    bitmap = iconAsset!!.fullColor,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                )
                            } else {
                                Icon(
                                    imageVector = InvokeIcons.Apps,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        },
                        content = {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun LiveStudioPreviewBanner(settings: LauncherSettings) {
    val inPreview = LocalInspectionMode.current
    val previewClockFamily = remember(settings.fontPreset, inPreview) {
        googleFontFamily(
            googleFontName = settings.fontPreset.clockFontName,
            inPreview = inPreview,
            isSerifFallback = settings.fontPreset.clockIsSerif,
        )
    }

    val customColor = settings.clockCustomColorArgb?.let(::Color)
    val accentColor = customColor ?: when (settings.clockTone) {
        TileTone.SURFACE -> MaterialTheme.colorScheme.onSurface
        TileTone.PRIMARY -> MaterialTheme.colorScheme.primary
        TileTone.SECONDARY -> MaterialTheme.colorScheme.secondary
        TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiary
    }
    val containerColor = customColor ?: when (settings.clockTone) {
        TileTone.SURFACE -> MaterialTheme.colorScheme.surfaceContainerHigh
        TileTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        TileTone.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        TileTone.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val onContainerColor = if (customColor != null) {
        if (customColor.luminance() > 0.48f) Color(0xFF141414) else Color.White
    } else {
        when (settings.clockTone) {
            TileTone.SURFACE -> MaterialTheme.colorScheme.onSurface
            TileTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
            TileTone.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
            TileTone.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
        }
    }
    val hourPreview = if (settings.clock24Hour) "09" else "9"
    val timePreview = buildString {
        append(hourPreview)
        append(":41")
        if (settings.showClockSeconds) append(":08")
        if (!settings.clock24Hour && settings.showClockAmPm) append(" AM")
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (settings.effectiveAmoledBlack) {
            Color.Black
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!settings.showClock) {
                    Text(
                        text = "Clock hidden",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    when (settings.clockStyle) {
                        ClockStyle.EDITORIAL_STACKED -> {
                            Text(
                                text = timePreview,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = previewClockFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 30.sp,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = accentColor,
                            )
                        }
                        ClockStyle.KINETIC_SPLIT -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor,
                            ) {
                                Text(
                                    text = hourPreview,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = previewClockFamily,
                                        fontWeight = FontWeight.Black,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = onContainerColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                Text(
                                    text = if (settings.showClockSeconds) "41:08" else "41",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = previewClockFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                        ClockStyle.SHAPE_DIAL -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(settings.clockShape.toContainerShape(isHero = true))
                                    .background(containerColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$hourPreview:41",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = previewClockFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum",
                                    ),
                                    color = onContainerColor,
                                )
                            }
                        }
                        ClockStyle.MINIMAL_INLINE -> {
                            Text(
                                text = timePreview,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = previewClockFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = accentColor,
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = settings.fontPreset.pairingSummary,
                    style = MaterialTheme.typography.labelSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        content()
    }
}

@Composable
private fun <T> ConnectedToggleSelector(
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            val shapes = when {
                options.size == 1 -> FilledTonalToggleButtonDefaults.shapesFor(
                    FilledTonalToggleButtonDefaults.size,
                )
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            FilledTonalToggleButton(
                checked = isSelected,
                onCheckedChange = { if (it) onSelect(option) },
                shapes = shapes,
                colors = FilledTonalToggleButtonDefaults.colors(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
            ) {
                Text(
                    text = labelOf(option),
                    style = if (isSelected) {
                        MaterialTheme.typography.labelLargeEmphasized
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PaletteSwatchOption(
    label: String,
    swatchColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val morphProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "paletteMorph",
    )
    val swatchShape = rememberMorphShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Cookie4Sided,
        progress = morphProgress,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .semantics { this.selected = selected }
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(swatchShape)
                .background(swatchColor),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = InvokeIcons.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = label,
            style = if (selected) {
                MaterialTheme.typography.labelSmallEmphasized
            } else {
                MaterialTheme.typography.labelSmall
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun DoubleTapToSleepControls(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var serviceEnabled by remember { mutableStateOf(ScreenLock.isServiceEnabled(context)) }
    LifecycleResumeEffect(Unit) {
        serviceEnabled = ScreenLock.isServiceEnabled(context)
        onPauseOrDispose {}
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StudioSwitchRow(
            title = "Double-Tap to Sleep",
            subtitle = "Double-tap empty space on Home to turn the screen off",
            checked = enabled,
            shape = RoundedCornerShape(20.dp),
            onCheckedChange = {
                onToggle(it)
                if (it && !serviceEnabled) ScreenLock.openSettings(context)
            },
        )
        if (enabled && !serviceEnabled) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { ScreenLock.openSettings(context) },
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Turn on the Invoke accessibility service",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "It only turns the screen off. If the switch is greyed out, open App info and allow restricted settings first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationBadgeControls(
    style: BadgeStyle,
    onSelect: (BadgeStyle) -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var accessGranted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }
    LifecycleResumeEffect(Unit) {
        accessGranted = NotificationAccess.isGranted(context)
        onPauseOrDispose {}
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Notification Badges",
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        ConnectedToggleSelector(
            options = BadgeStyle.entries,
            selected = style,
            labelOf = { it.label },
            onSelect = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelect(it)
                if (it != BadgeStyle.OFF && !accessGranted) NotificationAccess.openSettings(context)
            },
        )
        if (style != BadgeStyle.OFF && !accessGranted) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { NotificationAccess.openSettings(context) },
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Allow notification access",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Badges need it. If the switch is greyed out, open App info and allow restricted settings first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    shape: RoundedCornerShape,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        }
    }
}

@Preview(
    name = "Customization Studio · Dark",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun CustomizationStudioPreview() {
    val sampleSettings = LauncherSettings(
        darkMode = DarkMode.DARK,
        dynamicColor = false,
        palette = ExpressivePalette.TANGERINE,
        clockStyle = ClockStyle.KINETIC_SPLIT,
        fontPreset = LauncherFontPreset.GOOGLE_SANS_FLEX,
    )
    InvokeTheme(settings = sampleSettings) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxSize(),
        ) {
            CustomizationStudioContent(
                settings = sampleSettings,
                actions = StudioActions(),
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp),
            )
        }
    }
}
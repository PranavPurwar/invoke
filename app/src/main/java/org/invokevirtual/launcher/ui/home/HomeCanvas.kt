package org.invokevirtual.launcher.ui.home

import android.content.Intent
import android.graphics.Rect
import android.provider.AlarmClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.FolderLayoutMode
import org.invokevirtual.launcher.data.HomeCanvasState
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.QuickContact
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.TileTone
import org.invokevirtual.launcher.data.WidgetHeightTier
import org.invokevirtual.launcher.data.WorkspaceMode
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.ContactsPickerHelper
import org.invokevirtual.launcher.system.DoubleTapAction
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.ScreenLock
import org.invokevirtual.launcher.system.ScreenLockService
import org.invokevirtual.launcher.system.WidgetHostController
import org.invokevirtual.launcher.system.doubleTapAction
import org.invokevirtual.launcher.ui.components.AppActionSheet
import org.invokevirtual.launcher.ui.invoke.InvokeFilter
import org.invokevirtual.launcher.ui.invoke.InvokeSheet
import org.invokevirtual.launcher.ui.invoke.InvokeUiState
import org.invokevirtual.launcher.ui.invoke.QuickContactActionSheet
import org.invokevirtual.launcher.ui.studio.CustomizationStudioSheet
import org.invokevirtual.launcher.ui.studio.StudioActions
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.widgets.WidgetPickerSheet
import java.util.UUID
import kotlin.math.abs

@Immutable
data class HomeCanvasActions(
    val onAddApp: (InstalledApp, TileSpan, TileShapePreset, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _, _ -> },
    val onAddShortcut: (AppShortcutItem, TileSpan, TileShapePreset, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _, _ -> },
    val onRemoveShortcut: (String, String, Long, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onAddContact: (QuickContact, TileSpan, TileShapePreset, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onRemoveContact: (String, List<CanvasBlock>) -> Unit = { _, _ -> },
    val onAddSpacer: (String, TileSpan, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _ -> },
    val onAddFolder: (String, List<AppKey>, TileSpan, TileShapePreset, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _, _, _ -> },
    val onUpdateFolder: (String, String, List<AppKey>, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onAddAppToFolder: (String, AppKey, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onRemoveAppFromFolder: (String, AppKey, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetFolderLayout: (String, FolderLayoutMode, Int, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onAddSpacerToFolder: (String, String, TileSpan, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _, _ -> },
    val onRemoveFolderItem: (String, String, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onMoveFolderItem: (String, String, Int, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetFolderItemSpan: (String, String, TileSpan, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetFolderItemCustomSize: (String, String, Int, Int, List<CanvasBlock>) -> Unit = { _, _, _, _, _ -> },
    val onSetFolderItemShape: (String, String, TileShapePreset, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetFolderItemTone: (String, String, TileTone, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetFolderItemCustomBgColor: (String, String, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onAddWidget: (Int, WidgetHeightTier, TileShapePreset, Int?, Int?, List<CanvasBlock>) -> Unit = { _, _, _, _, _, _ -> },
    val onRemoveBlock: (CanvasBlock, List<CanvasBlock>) -> Unit = { _, _ -> },
    val onMoveBlock: (String, Int, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetTileSpan: (String, TileSpan, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetTileCustomSize: (String, Int, Int, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetWidgetSize: (String, WidgetHeightTier, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetWidgetCustomSize: (String, Int, Int, List<CanvasBlock>) -> Unit = { _, _, _, _ -> },
    val onSetShape: (String, TileShapePreset, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetTone: (String, TileTone, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onSetCustomBgColor: (String, Int?, List<CanvasBlock>) -> Unit = { _, _, _ -> },
    val onResetDefaults: () -> Unit = {},
)

@Composable
fun HomeCanvas(
    settings: LauncherSettings,
    canvasState: HomeCanvasState,
    installedApps: List<InstalledApp>,
    appCatalog: AppCatalog?,
    onSelectWorkspaceMode: (WorkspaceMode) -> Unit,
    onLaunchApp: (InstalledApp, Rect?) -> Unit,
    modifier: Modifier = Modifier,
    onLaunchShortcut: (String, String, Long, Rect?) -> Unit = { pkg, id, serial, bounds ->
        appCatalog?.startShortcutById(pkg, id, serial, bounds)
    },
    canvasActions: HomeCanvasActions = HomeCanvasActions(),
    invokeUiState: InvokeUiState = InvokeUiState(),
    studioActions: StudioActions = StudioActions(),
    widgetHost: WidgetHostController? = null,
    initialEditMode: Boolean = false,
    onInvokeQueryChange: (String) -> Unit = {},
    onInvokeClearQuery: () -> Unit = {},
    onInvokeSelectFilter: (InvokeFilter) -> Unit = {},
    onTogglePrivateSpace: (Boolean) -> Unit = {},
    onSavePickedContacts: (List<QuickContact>) -> Unit = {},
    onRemoveQuickContact: (String) -> Unit = {},
    onTogglePinInDrawer: (AppKey) -> Unit = {},
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val inPreview = LocalInspectionMode.current
    val reducedMotion = LocalReducedMotion.current
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val isMedium = !isExpanded &&
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    var isEditMode by rememberSaveable { mutableStateOf(initialEditMode) }
    var selectedBlockId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingSelectId by remember { mutableStateOf<String?>(null) }
    var liveWidgetResizePreview by remember { mutableStateOf<Pair<String, Pair<Int, Int>>?>(null) }
    var modeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var invokeSheetOpen by rememberSaveable { mutableStateOf(false) }
    var invokeSheetAutoFocus by rememberSaveable { mutableStateOf(false) }
    var studioOpen by rememberSaveable { mutableStateOf(false) }
    var widgetPickerOpen by rememberSaveable { mutableStateOf(false) }
    var addAppSheetOpen by rememberSaveable { mutableStateOf(false) }
    var openFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var manageFolderAppsId by rememberSaveable { mutableStateOf<String?>(null) }
    var actionTargetApp by remember { mutableStateOf<InstalledApp?>(null) }
    var actionTargetFromDrawer by remember { mutableStateOf(false) }
    var actionTargetContact by remember { mutableStateOf<QuickContact?>(null) }

    val appsByKey = remember(installedApps) { installedApps.associateBy { it.key } }
    val defaultBlocks = remember(installedApps) { buildDefaultCanvasBlocks(installedApps) }
    val effectiveBlocks = remember(canvasState, defaultBlocks) {
        if (canvasState.isCustomized) canvasState.blocks else defaultBlocks
    }
    val foldersList = remember(effectiveBlocks) {
        effectiveBlocks.filterIsInstance<CanvasBlock.FolderTile>()
    }
    val visibleBlocks = remember(effectiveBlocks, settings.workspaceMode, isEditMode) {
        if (settings.workspaceMode == WorkspaceMode.DEEP_FOCUS && !isEditMode) {
            effectiveBlocks.take(4)
        } else {
            effectiveBlocks
        }
    }

    var previousBlockIds by remember { mutableStateOf(effectiveBlocks.map { it.id }.toSet()) }

    LaunchedEffect(isEditMode, effectiveBlocks) {
        val currentIds = effectiveBlocks.map { it.id }.toSet()
        val newlyAddedId = (currentIds - previousBlockIds).firstOrNull()
        previousBlockIds = currentIds

        if (!isEditMode) {
            selectedBlockId = null
            pendingSelectId = null
            liveWidgetResizePreview = null
        } else if (newlyAddedId != null) {
            selectedBlockId = newlyAddedId
            pendingSelectId = null
        } else if (pendingSelectId != null && effectiveBlocks.any { it.id == pendingSelectId }) {
            selectedBlockId = pendingSelectId
            pendingSelectId = null
        } else if (selectedBlockId != null && effectiveBlocks.none { it.id == selectedBlockId }) {
            selectedBlockId = null
        }
    }
    LaunchedEffect(selectedBlockId) {
        if (liveWidgetResizePreview?.first != selectedBlockId) {
            liveWidgetResizePreview = null
        }
    }

    val openInvokeSheet: (Boolean) -> Unit = { autoFocus ->
        if (autoFocus) {
            onInvokeSelectFilter(InvokeFilter.ALL)
        }
        invokeSheetAutoFocus = autoFocus
        invokeSheetOpen = true
    }
    val closeInvokeSheet: () -> Unit = {
        invokeSheetOpen = false
        onInvokeClearQuery()
    }

    val backState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = actionTargetApp != null ||
            actionTargetContact != null ||
                manageFolderAppsId != null ||
                openFolderId != null ||
            addAppSheetOpen ||
            widgetPickerOpen ||
            studioOpen ||
            modeMenuExpanded ||
            isEditMode ||
            invokeSheetOpen,
        onBackCompleted = {
            when {
                actionTargetApp != null -> {
                    actionTargetApp = null
                    actionTargetFromDrawer = false
                }
                actionTargetContact != null -> actionTargetContact = null
                manageFolderAppsId != null -> manageFolderAppsId = null
                openFolderId != null -> openFolderId = null
                addAppSheetOpen -> addAppSheetOpen = false
                widgetPickerOpen -> widgetPickerOpen = false
                studioOpen -> studioOpen = false
                modeMenuExpanded -> modeMenuExpanded = false
                isEditMode -> isEditMode = false
                invokeSheetOpen -> closeInvokeSheet()
            }
        },
    )

    val quickActions = remember(installedApps, settings.dockApps, appsByKey) {
        buildDockQuickActions(
            customDockKeys = settings.dockApps,
            appsByKey = appsByKey,
            installedApps = installedApps,
            onLaunch = { app -> onLaunchApp(app, null) },
            onLongPressApp = { app ->
                actionTargetFromDrawer = false
                actionTargetApp = app
            },
            onFallbackSearch = { openInvokeSheet(true) },
        )
    }

    val openClockApp: () -> Unit = {
        if (!inPreview) {
            runCatching {
                context.startActivity(
                    Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }

    val cycleWorkspaceMode: () -> Unit = {
        val entries = WorkspaceMode.entries
        val next = entries[(entries.indexOf(settings.workspaceMode) + 1) % entries.size]
        onSelectWorkspaceMode(next)
    }

    val scrimModifier = if (settings.effectiveAmoledBlack) {
        Modifier.background(Color.Black)
    } else if (inPreview) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceContainerLowest,
                    MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ),
        )
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
                    0.35f to Color.Transparent,
                    0.74f to Color.Transparent,
                    1.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.32f),
                ),
            ),
        )
    }

    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val doubleTapExclusions = remember { DoubleTapExclusions() }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    val gridState = rememberLazyGridState()
    val swipeThresholdPx = with(LocalDensity.current) { 48.dp.toPx() }
    var overscrollDeltaY by remember { mutableFloatStateOf(0f) }
    var scrollConsumedInGesture by remember { mutableFloatStateOf(0f) }
    var drawerTriggeredInGesture by remember { mutableStateOf(false) }

    val openStatusBar = remember(context) {
        {
            runCatching {
                val service = context.getSystemService("statusbar")
                val statusBarManager = Class.forName("android.app.StatusBarManager")
                val expand = statusBarManager.getMethod("expandNotificationsPanel")
                expand.invoke(service)
            }
        }
    }

    val drawerSwipeNestedScroll = remember(isEditMode, invokeSheetOpen, swipeThresholdPx) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (isEditMode || invokeSheetOpen || drawerTriggeredInGesture) {
                    return Offset.Zero
                }
                if (source == NestedScrollSource.UserInput) {
                    scrollConsumedInGesture += abs(consumed.y)
                    if (scrollConsumedInGesture < 8f && abs(available.y) > 0f) {
                        overscrollDeltaY += available.y
                        if (overscrollDeltaY < -swipeThresholdPx) {
                            drawerTriggeredInGesture = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            openInvokeSheet(false)
                            return available
                        } else if (overscrollDeltaY > swipeThresholdPx) {
                            drawerTriggeredInGesture = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            openStatusBar()
                            return available
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!isEditMode && !invokeSheetOpen && !drawerTriggeredInGesture &&
                    scrollConsumedInGesture < 8f
                ) {
                    val fastSwipeDown = !gridState.canScrollBackward && available.y > 900f
                    val fastSwipeUp = !gridState.canScrollForward && available.y < -900f
                    if (fastSwipeUp) {
                        drawerTriggeredInGesture = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openInvokeSheet(false)
                        return available
                    } else if (fastSwipeDown) {
                        drawerTriggeredInGesture = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openStatusBar()
                        return available
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(scrimModifier)
            .onGloballyPositioned { rootOrigin = it.positionInRoot() }
            .pointerInput(isEditMode, settings.doubleTapToSleep) {
                detectTapGestures(
                    onDoubleTap = if (settings.doubleTapToSleep) {
                        { offset ->
                            val action = doubleTapAction(
                                enabled = true,
                                supported = ScreenLock.isSupported,
                                excluded = doubleTapExclusions.contains(offset + rootOrigin),
                                isEditMode = isEditMode,
                                serviceConnected = ScreenLockService.isConnected,
                            )
                            when (action) {
                                DoubleTapAction.LOCK -> ScreenLockService.lock()
                                DoubleTapAction.OPEN_SETTINGS -> ScreenLock.openSettings(context)
                                DoubleTapAction.NONE -> Unit
                            }
                        }
                    } else {
                        null
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isEditMode = !isEditMode
                    },
                )
            }
            .pointerInput(isEditMode, invokeSheetOpen) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        if (!isEditMode && !invokeSheetOpen) {
                            if (dragAccumulator < -swipeThresholdPx) {
                                openInvokeSheet(false)
                            } else if (dragAccumulator > swipeThresholdPx) {
                                openStatusBar()
                            }
                        }
                        dragAccumulator = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    },
                )
            },
    ) {
        val gridColumns = when {
            isExpanded -> 6
            isMedium -> 5
            else -> 4
        }
        val horizontalPadding = if (isMedium || isExpanded) 28.dp else 20.dp
        val itemPlacementSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
        val selectedIndex = visibleBlocks.indexOfFirst { it.id == selectedBlockId }
        val selectedBlock = visibleBlocks.getOrNull(selectedIndex)
        val hasHeaderContent = settings.showClock ||
            settings.showGlanceBattery ||
            settings.showGlanceAlarm ||
            settings.showClockModeBadge
        val layoutDirection = LocalLayoutDirection.current
        val statusTopInset = WindowInsets.statusBars
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()
        val horizontalCutoutPadding = WindowInsets.displayCutout
            .only(WindowInsetsSides.Horizontal)
            .asPaddingValues()
        val navBottomInset = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isExpanded && settings.showDock) {
                InvokeVerticalDock(
                    quickActions = quickActions,
                    activeMode = settings.workspaceMode,
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode },
                    onAddWidgetClick = { widgetPickerOpen = true },
                    onOpenStudio = { studioOpen = true },
                    onCycleMode = cycleWorkspaceMode,
                    onInvokeSearchClick = { openInvokeSheet(true) },
                    modifier = Modifier
                        .systemBarsPadding()
                        .displayCutoutPadding()
                        .padding(start = 24.dp, end = 8.dp)
                        .excludeFromDoubleTap(doubleTapExclusions, "dock"),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            overscrollDeltaY = 0f
                            scrollConsumedInGesture = 0f
                            drawerTriggeredInGesture = false
                        }
                    }
                    .nestedScroll(drawerSwipeNestedScroll),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = horizontalPadding + horizontalCutoutPadding.calculateStartPadding(layoutDirection),
                        end = horizontalPadding + horizontalCutoutPadding.calculateEndPadding(layoutDirection),
                        top = statusTopInset + 14.dp,
                        bottom = navBottomInset + when {
                            isEditMode -> 220.dp
                            isExpanded || !settings.showDock -> 32.dp
                            else -> 112.dp
                        },
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (hasHeaderContent) {
                        item(
                            key = "timepiece_monument",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            TimepieceMonument(
                                settings = settings,
                                onClockClick = openClockApp,
                                onSelectMode = onSelectWorkspaceMode,
                                onOpenCustomize = { isEditMode = true },
                                modifier = Modifier.padding(bottom = 14.dp),
                            )
                        }
                    }

                    if (isEditMode) {
                        item(
                            key = "edit_mode_banner",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Editing Home Canvas",
                                            style = MaterialTheme.typography.titleSmallEmphasized,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                        Text(
                                            text = "Tap any item to change size, shape, color, or position",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (canvasState.isCustomized) {
                                        TextButton(
                                            onClick = canvasActions.onResetDefaults,
                                            modifier = Modifier.heightIn(min = 48.dp),
                                        ) {
                                            Text(
                                                text = "Reset",
                                                style = MaterialTheme.typography.labelLargeEmphasized,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(
                        items = visibleBlocks,
                        key = { _, block -> block.id },
                        span = { _, block ->
                            val overrideCols = liveWidgetResizePreview
                                ?.takeIf { it.first == block.id }
                                ?.second
                                ?.first
                            val cols = when (block) {
                                is CanvasBlock.AppTile -> overrideCols ?: block.effectiveColumns
                                is CanvasBlock.ShortcutTile -> overrideCols ?: block.effectiveColumns
                                is CanvasBlock.ContactTile -> overrideCols ?: block.effectiveColumns
                                is CanvasBlock.SpacerBlock -> overrideCols ?: block.effectiveColumns
                                is CanvasBlock.FolderTile -> overrideCols ?: block.effectiveColumns
                                is CanvasBlock.WidgetBlock -> overrideCols ?: block.effectiveColumns
                            }.coerceAtMost(maxLineSpan)
                            GridItemSpan(cols)
                        },
                    ) { index, block ->
                        val animateMod = if (reducedMotion) {
                            Modifier
                        } else {
                            Modifier.animateItem(placementSpec = itemPlacementSpec)
                        }
                        val isBlockSelected = isEditMode && block.id == selectedBlockId
                        val previewOverride = liveWidgetResizePreview
                            ?.takeIf { it.first == block.id }
                            ?.second

                        when (block) {
                            is CanvasBlock.AppTile -> {
                                val resolvedApp = appsByKey[block.appKey]
                                val fallback = fallbackMetaFor(block, index)
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                CanvasAppBlockCard(
                                    block = displayBlock,
                                    app = resolvedApp,
                                    fallbackLabel = fallback.first,
                                    fallbackGlyph = fallback.second,
                                    iconStyle = settings.iconStyle,
                                    showLabel = settings.showLabels,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    appCatalog = appCatalog,
                                    onClick = { bounds ->
                                        if (resolvedApp != null) {
                                            onLaunchApp(resolvedApp, bounds)
                                        } else {
                                            openInvokeSheet(false)
                                        }
                                    },
                                    onLongClick = {
                                        if (resolvedApp != null) {
                                            actionTargetFromDrawer = false
                                            actionTargetApp = resolvedApp
                                        } else {
                                            isEditMode = true
                                            selectedBlockId = block.id
                                        }
                                    },
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod,
                                )
                            }

                            is CanvasBlock.ShortcutTile -> {
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                CanvasShortcutBlockCard(
                                    block = displayBlock,
                                    showLabel = settings.showLabels,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    appCatalog = appCatalog,
                                    onClick = { bounds ->
                                        onLaunchShortcut(
                                            block.packageName,
                                            block.shortcutId,
                                            block.userSerial,
                                            bounds,
                                        )
                                    },
                                    onLongClick = {
                                        isEditMode = true
                                        selectedBlockId = block.id
                                    },
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod,
                                )
                            }

                            is CanvasBlock.ContactTile -> {
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                val contact = QuickContact(
                                    lookupKey = block.lookupKey,
                                    displayName = block.displayName,
                                    phoneNumber = block.phoneNumber,
                                )
                                CanvasContactBlockCard(
                                    block = displayBlock,
                                    showLabel = settings.showLabels,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    onClick = { ContactsPickerHelper.dialContact(context, contact) },
                                    onLongClick = { actionTargetContact = contact },
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod,
                                )
                            }

                            is CanvasBlock.FolderTile -> {
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                val folderApps = remember(block.apps, appsByKey) {
                                    block.apps.mapNotNull { appsByKey[it] }
                                }
                                CanvasFolderBlockCard(
                                    block = displayBlock,
                                    folderApps = folderApps,
                                    iconStyle = settings.iconStyle,
                                    showLabel = settings.showLabels,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    appCatalog = appCatalog,
                                    onClick = { openFolderId = block.id },
                                    onLongClick = {
                                        isEditMode = true
                                        selectedBlockId = block.id
                                    },
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod,
                                )
                            }

                            is CanvasBlock.SpacerBlock -> {
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                CanvasSpacerBlockCard(
                                    block = displayBlock,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod,
                                )
                            }

                            is CanvasBlock.WidgetBlock -> {
                                val displayBlock = if (previewOverride != null) {
                                    block.copy(
                                        customColumns = previewOverride.first,
                                        customHeightDp = previewOverride.second,
                                    )
                                } else {
                                    block
                                }
                                CanvasWidgetBlockCard(
                                    block = displayBlock,
                                    widgetHost = widgetHost,
                                    isEditMode = isEditMode,
                                    isSelected = isBlockSelected,
                                    onSelectInEdit = { selectedBlockId = block.id },
                                    modifier = animateMod.excludeFromDoubleTap(doubleTapExclusions, block.id),
                                )
                            }
                        }
                    }

                    if (settings.workspaceMode == WorkspaceMode.DEEP_FOCUS &&
                        !isEditMode &&
                        effectiveBlocks.size > visibleBlocks.size
                    ) {
                        item(
                            key = "deep_focus_footer",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            Surface(
                                onClick = { onSelectWorkspaceMode(WorkspaceMode.EVERYDAY) },
                                shape = RoundedCornerShape(percent = 50),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.84f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Icon(
                                        imageVector = InvokeIcons.Focus,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Deep Focus · ${effectiveBlocks.size - visibleBlocks.size} more tucked away · Tap for Everyday",
                                        style = MaterialTheme.typography.labelLargeEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                if (isEditMode || (!isExpanded && settings.showDock)) {
                    val spatialOffsetSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
                    val effectsFloatSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(if (isMedium || isExpanded) 0.72f else 1f)
                            .navigationBarsPadding()
                            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                            .padding(horizontal = horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = isEditMode && selectedBlock != null,
                            enter = fadeIn(effectsFloatSpec) + slideInVertically(spatialOffsetSpec) { it / 2 },
                            exit = fadeOut(effectsFloatSpec) + slideOutVertically(spatialOffsetSpec) { it / 2 },
                        ) {
                            selectedBlock?.let { activeBlock ->
                                val title = when (activeBlock) {
                                    is CanvasBlock.AppTile -> appsByKey[activeBlock.appKey]?.label
                                        ?: fallbackMetaFor(activeBlock, selectedIndex.coerceAtLeast(0)).first
                                    is CanvasBlock.ShortcutTile -> activeBlock.label
                                    is CanvasBlock.ContactTile -> activeBlock.displayName
                                    is CanvasBlock.SpacerBlock -> "Empty Space"
                                    is CanvasBlock.FolderTile -> activeBlock.title.ifBlank { "Folder" }
                                    is CanvasBlock.WidgetBlock -> "Widget"
                                }
                                val previewOverride = liveWidgetResizePreview
                                    ?.takeIf { it.first == activeBlock.id }
                                    ?.second
                                val sizeLabel = when (activeBlock) {
                                    is CanvasBlock.AppTile -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }
                                    is CanvasBlock.ShortcutTile -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }
                                    is CanvasBlock.ContactTile -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }

                                    is CanvasBlock.SpacerBlock -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }

                                    is CanvasBlock.FolderTile -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }
                                    is CanvasBlock.WidgetBlock -> if (previewOverride != null) {
                                        "${previewOverride.first}×${previewOverride.second}dp"
                                    } else {
                                        activeBlock.sizeLabel
                                    }
                                }
                                val currentTileSpan = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.span
                                    is CanvasBlock.ShortcutTile -> activeBlock.span
                                    is CanvasBlock.ContactTile -> activeBlock.span
                                    is CanvasBlock.SpacerBlock -> activeBlock.span
                                    is CanvasBlock.FolderTile -> activeBlock.span
                                    is CanvasBlock.WidgetBlock -> null
                                }
                                val widgetBlock = activeBlock as? CanvasBlock.WidgetBlock
                                val folderBlock = activeBlock as? CanvasBlock.FolderTile
                                val currentWidgetSize = widgetBlock?.heightTier
                                val activeBlockCols = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.effectiveColumns
                                    is CanvasBlock.ShortcutTile -> activeBlock.effectiveColumns
                                    is CanvasBlock.ContactTile -> activeBlock.effectiveColumns
                                    is CanvasBlock.SpacerBlock -> activeBlock.effectiveColumns
                                    is CanvasBlock.FolderTile -> activeBlock.effectiveColumns
                                    is CanvasBlock.WidgetBlock -> activeBlock.effectiveColumns
                                }
                                val activeBlockHeightDp = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.effectiveHeightDp
                                    is CanvasBlock.ShortcutTile -> activeBlock.effectiveHeightDp
                                    is CanvasBlock.ContactTile -> activeBlock.effectiveHeightDp
                                    is CanvasBlock.SpacerBlock -> activeBlock.effectiveHeightDp
                                    is CanvasBlock.FolderTile -> activeBlock.effectiveHeightDp
                                    is CanvasBlock.WidgetBlock -> activeBlock.effectiveHeightDp
                                }
                                val activeHasCustomSize = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.hasCustomSize
                                    is CanvasBlock.ShortcutTile -> activeBlock.hasCustomSize
                                    is CanvasBlock.ContactTile -> activeBlock.hasCustomSize
                                    is CanvasBlock.SpacerBlock -> activeBlock.hasCustomSize
                                    is CanvasBlock.FolderTile -> activeBlock.hasCustomSize
                                    is CanvasBlock.WidgetBlock -> activeBlock.hasCustomSize
                                }
                                val currentTone = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.tone
                                    is CanvasBlock.ShortcutTile -> activeBlock.tone
                                    is CanvasBlock.ContactTile -> activeBlock.tone
                                    is CanvasBlock.SpacerBlock -> null
                                    is CanvasBlock.FolderTile -> activeBlock.tone
                                    is CanvasBlock.WidgetBlock -> null
                                }
                                val currentCustomBgArgb = when (activeBlock) {
                                    is CanvasBlock.AppTile -> activeBlock.customBgArgb
                                    is CanvasBlock.ShortcutTile -> activeBlock.customBgArgb
                                    is CanvasBlock.ContactTile -> activeBlock.customBgArgb
                                    is CanvasBlock.SpacerBlock -> null
                                    is CanvasBlock.FolderTile -> activeBlock.customBgArgb
                                    is CanvasBlock.WidgetBlock -> null
                                }

                                CanvasBlockInspectorBar(
                                    blockId = activeBlock.id,
                                    title = title,
                                    sizeLabel = sizeLabel,
                                    currentShape = activeBlock.shape,
                                    currentTileSpan = currentTileSpan,
                                    currentWidgetSize = currentWidgetSize,
                                    currentWidgetColumns = previewOverride?.first ?: activeBlockCols,
                                    currentWidgetHeightDp = previewOverride?.second ?: activeBlockHeightDp,
                                    hasCustomWidgetSize = previewOverride != null || activeHasCustomSize,
                                    currentTone = currentTone,
                                    currentCustomBgArgb = currentCustomBgArgb,
                                    canMoveBack = selectedIndex > 0,
                                    canMoveForward = selectedIndex in 0 until visibleBlocks.lastIndex,
                                    supportsShape = activeBlock !is CanvasBlock.SpacerBlock,
                                    minCustomTileHeightDp = if (activeBlock is CanvasBlock.SpacerBlock) 24f else 64f,
                                    maxCustomTileHeightDp = if (activeBlock is CanvasBlock.SpacerBlock) 360f else 280f,
                                    onMoveBack = {
                                        canvasActions.onMoveBlock(activeBlock.id, -1, effectiveBlocks)
                                    },
                                    onMoveForward = {
                                        canvasActions.onMoveBlock(activeBlock.id, +1, effectiveBlocks)
                                    },
                                    onSelectTileSpan = { span ->
                                        liveWidgetResizePreview = null
                                        canvasActions.onSetTileSpan(activeBlock.id, span, effectiveBlocks)
                                    },
                                    onSelectWidgetSize = { tier ->
                                        liveWidgetResizePreview = null
                                        canvasActions.onSetWidgetSize(activeBlock.id, tier, effectiveBlocks)
                                    },
                                    onPreviewWidgetCustomSize = { cols, heightDp ->
                                        liveWidgetResizePreview = activeBlock.id to (cols to heightDp)
                                    },
                                    onSelectWidgetCustomSize = { cols, heightDp ->
                                        liveWidgetResizePreview = null
                                        if (activeBlock is CanvasBlock.WidgetBlock) {
                                            canvasActions.onSetWidgetCustomSize(activeBlock.id, cols, heightDp, effectiveBlocks)
                                        } else {
                                            canvasActions.onSetTileCustomSize(activeBlock.id, cols, heightDp, effectiveBlocks)
                                        }
                                    },
                                    onSelectShape = { shape ->
                                        canvasActions.onSetShape(activeBlock.id, shape, effectiveBlocks)
                                    },
                                    onSelectTone = { tone ->
                                        canvasActions.onSetTone(activeBlock.id, tone, effectiveBlocks)
                                    },
                                    onSelectCustomBgArgb = { argb ->
                                        canvasActions.onSetCustomBgColor(activeBlock.id, argb, effectiveBlocks)
                                    },
                                    onEditWidget = if (widgetBlock != null && widgetHost != null) {
                                        val info = widgetHost.getWidgetInfo(widgetBlock.appWidgetId)
                                        if (info?.configure != null) {
                                            {
                                                val configIntent = widgetHost.createConfigureIntent(
                                                    widgetBlock.appWidgetId,
                                                    info,
                                                )
                                                if (configIntent != null) {
                                                    runCatching {
                                                        context.startActivity(
                                                            configIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            null
                                        }
                                    } else {
                                        null
                                    },
                                    onEditFolder = if (folderBlock != null) {
                                        { manageFolderAppsId = folderBlock.id }
                                    } else {
                                        null
                                    },
                                    onRemove = {
                                        liveWidgetResizePreview = null
                                        canvasActions.onRemoveBlock(activeBlock, effectiveBlocks)
                                        selectedBlockId = null
                                    },
                                )
                            }
                        }

                        if (!isExpanded) {
                            AnimatedContent(
                                targetState = isEditMode,
                                transitionSpec = {
                                    (fadeIn(effectsFloatSpec) + slideInVertically(spatialOffsetSpec) { it / 3 })
                                        .togetherWith(
                                            fadeOut(effectsFloatSpec) + slideOutVertically(spatialOffsetSpec) { it / 3 },
                                        )
                                },
                                label = "dockModeMorph",
                            ) { editing ->
                                if (editing) {
                                    CanvasEditDock(
                                        onAddWidgetClick = { widgetPickerOpen = true },
                                        onAddAppClick = { addAppSheetOpen = true },
                                        onOpenStudio = { studioOpen = true },
                                        onDone = { isEditMode = false },
                                    )
                                } else if (settings.showDock) {
                                    InvokeBottomDock(
                                        quickActions = quickActions,
                                        dockStyle = settings.dockStyle,
                                        showModeButton = settings.showDockModeButton,
                                        iconStyle = settings.iconStyle,
                                        appCatalog = appCatalog,
                                        activeMode = settings.workspaceMode,
                                        modeMenuExpanded = modeMenuExpanded,
                                        onModeMenuToggle = { modeMenuExpanded = it },
                                        onSelectMode = onSelectWorkspaceMode,
                                        onEnterEditMode = { isEditMode = true },
                                        onAddWidgetClick = { widgetPickerOpen = true },
                                        onOpenStudio = { studioOpen = true },
                                        onInvokeSearchClick = { openInvokeSheet(true) },
                                        modifier = Modifier.excludeFromDoubleTap(doubleTapExclusions, "dock"),
                                    )
                                }
                            }
                        } else if (isEditMode) {
                            CanvasEditDock(
                                onAddWidgetClick = { widgetPickerOpen = true },
                                onAddAppClick = { addAppSheetOpen = true },
                                onOpenStudio = { studioOpen = true },
                                onDone = { isEditMode = false },
                            )
                        }
                    }
                }
            }
        }
    }

    if (invokeSheetOpen) {
        InvokeSheet(
            uiState = invokeUiState,
            autoFocusSearch = invokeSheetAutoFocus,
            iconStyle = settings.iconStyle,
            showQuickPeople = settings.showQuickPeople,
            drawerTransparency = settings.drawerTransparency,
            bottomSearchBar = settings.bottomSearchBar,
            appCatalog = appCatalog,
            onQueryChange = onInvokeQueryChange,
            onClearQuery = onInvokeClearQuery,
            onSelectFilter = onInvokeSelectFilter,
            onDismiss = closeInvokeSheet,
            onLaunchApp = { app ->
                closeInvokeSheet()
                onLaunchApp(app, null)
            },
            onLongPressApp = { app ->
                actionTargetFromDrawer = true
                actionTargetApp = app
            },
            onLaunchShortcut = { shortcut ->
                closeInvokeSheet()
                appCatalog?.startShortcut(shortcut)
            },
            onTogglePrivateSpace = onTogglePrivateSpace,
            onSavePickedContacts = onSavePickedContacts,
            onLongPressQuickContact = { contact ->
                actionTargetContact = contact
            },
        )
    }

    if (studioOpen) {
        CustomizationStudioSheet(
            settings = settings,
            actions = studioActions,
            installedApps = installedApps,
            appCatalog = appCatalog,
            onDismiss = { studioOpen = false },
        )
    }

    if (widgetPickerOpen) {
        WidgetPickerSheet(
            widgetHost = widgetHost,
            onWidgetPicked = { appWidgetId ->
                isEditMode = true
                pendingSelectId = "widget_$appWidgetId"
                selectedBlockId = "widget_$appWidgetId"
                canvasActions.onAddWidget(
                    appWidgetId,
                    WidgetHeightTier.MEDIUM,
                    TileShapePreset.SQUIRCLE,
                    null,
                    null,
                    effectiveBlocks,
                )
            },
            onDismiss = { widgetPickerOpen = false },
        )
    }

    if (addAppSheetOpen) {
        AddCanvasAppSheet(
            installedApps = installedApps,
            iconStyle = settings.iconStyle,
            appCatalog = appCatalog,
            onSelectApp = { app ->
                isEditMode = true
                canvasActions.onAddApp(
                    app,
                    TileSpan.COMPACT,
                    TileShapePreset.SQUIRCLE,
                    null,
                    null,
                    effectiveBlocks
                )
                addAppSheetOpen = false
            },
            onSelectShortcut = { shortcut ->
                isEditMode = true
                canvasActions.onAddShortcut(
                    shortcut,
                    TileSpan.COMPACT,
                    TileShapePreset.LEAF,
                    null,
                    null,
                    effectiveBlocks
                )
                addAppSheetOpen = false
            },
            onCreateFolder = { title, apps ->
                isEditMode = true
                canvasActions.onAddFolder(
                    title,
                    apps,
                    TileSpan.COMPACT,
                    TileShapePreset.SQUIRCLE,
                    null,
                    null,
                    effectiveBlocks
                )
                addAppSheetOpen = false
            },
            onSelectSpacer = { span, customCols, customHeightDp ->
                isEditMode = true
                val newId = "spc_${UUID.randomUUID().toString().take(8)}"
                pendingSelectId = newId
                selectedBlockId = newId
                canvasActions.onAddSpacer(newId, span, customCols, customHeightDp, effectiveBlocks)
                addAppSheetOpen = false
            },
            onDismiss = { addAppSheetOpen = false },
        )
    }

    openFolderId?.let { activeFolderId ->
        val activeFolder = foldersList.firstOrNull { it.id == activeFolderId }
        if (activeFolder != null) {
            FolderBottomSheet(
                folder = activeFolder,
                appsByKey = appsByKey,
                iconStyle = settings.iconStyle,
                showLabels = settings.showLabels,
                appCatalog = appCatalog,
                onDismiss = { openFolderId = null },
                actions = FolderSheetActions(
                    onManageApps = { manageFolderAppsId = activeFolder.id },
                    onSetLayout = { mode, cols ->
                        canvasActions.onSetFolderLayout(
                            activeFolder.id,
                            mode,
                            cols,
                            effectiveBlocks
                        )
                    },
                    onAddSpacer = { spacerId, span, cols, heightDp ->
                        canvasActions.onAddSpacerToFolder(
                            activeFolder.id,
                            spacerId,
                            span,
                            cols,
                            heightDp,
                            effectiveBlocks
                        )
                    },
                    onRemoveItem = { itemId ->
                        canvasActions.onRemoveFolderItem(activeFolder.id, itemId, effectiveBlocks)
                    },
                    onMoveItem = { itemId, delta ->
                        canvasActions.onMoveFolderItem(
                            activeFolder.id,
                            itemId,
                            delta,
                            effectiveBlocks
                        )
                    },
                    onSetItemSpan = { itemId, span ->
                        canvasActions.onSetFolderItemSpan(
                            activeFolder.id,
                            itemId,
                            span,
                            effectiveBlocks
                        )
                    },
                    onSetItemCustomSize = { itemId, cols, heightDp ->
                        canvasActions.onSetFolderItemCustomSize(
                            activeFolder.id,
                            itemId,
                            cols,
                            heightDp,
                            effectiveBlocks
                        )
                    },
                    onSetItemShape = { itemId, shape ->
                        canvasActions.onSetFolderItemShape(
                            activeFolder.id,
                            itemId,
                            shape,
                            effectiveBlocks
                        )
                    },
                    onSetItemTone = { itemId, tone ->
                        canvasActions.onSetFolderItemTone(
                            activeFolder.id,
                            itemId,
                            tone,
                            effectiveBlocks
                        )
                    },
                    onSetItemCustomBgColor = { itemId, argb ->
                        canvasActions.onSetFolderItemCustomBgColor(
                            activeFolder.id,
                            itemId,
                            argb,
                            effectiveBlocks
                        )
                    },
                ),
                onLaunchApp = { app, bounds ->
                    openFolderId = null
                    onLaunchApp(app, bounds)
                },
                onLongPressApp = { app ->
                    actionTargetFromDrawer = false
                    actionTargetApp = app
                },
            )
        }
    }

    manageFolderAppsId?.let { folderId ->
        val activeFolder = foldersList.firstOrNull { it.id == folderId }
        if (activeFolder != null) {
            FolderAppsPickerSheet(
                folder = activeFolder,
                installedApps = installedApps,
                iconStyle = settings.iconStyle,
                appCatalog = appCatalog,
                onSaveFolder = { newTitle, updatedKeys ->
                    canvasActions.onUpdateFolder(
                        activeFolder.id,
                        newTitle,
                        updatedKeys,
                        effectiveBlocks
                    )
                },
                onDismiss = { manageFolderAppsId = null },
            )
        }
    }

    actionTargetApp?.let { targetApp ->
        val activeFolder = openFolderId?.let { folderId ->
            foldersList.firstOrNull { it.id == folderId && targetApp.key in it.apps }
        }
        AppActionSheet(
            app = targetApp,
            folders = foldersList,
            activeFolder = activeFolder,
            isFromDrawer = actionTargetFromDrawer,
            isPinnedInDrawer = targetApp.key in invokeUiState.pinnedDrawerKeys,
            iconStyle = settings.iconStyle,
            appCatalog = appCatalog,
            onDismiss = {
                actionTargetApp = null
                actionTargetFromDrawer = false
            },
            onTogglePinInDrawer = {
                onTogglePinInDrawer(targetApp.key)
            },
            onToggleFolderMembership = { folder ->
                if (targetApp.key in folder.apps) {
                    canvasActions.onRemoveAppFromFolder(folder.id, targetApp.key, effectiveBlocks)
                } else {
                    canvasActions.onAddAppToFolder(folder.id, targetApp.key, effectiveBlocks)
                }
            },
            onCreateFolderWithApp = {
                canvasActions.onAddFolder(
                    targetApp.label,
                    listOf(targetApp.key),
                    TileSpan.COMPACT,
                    TileShapePreset.SQUIRCLE,
                    null,
                    null,
                    effectiveBlocks,
                )
            },
            onRemoveFromActiveFolder = {
                activeFolder?.let { folder ->
                    canvasActions.onRemoveAppFromFolder(folder.id, targetApp.key, effectiveBlocks)
                }
            },
            onLaunchShortcut = { shortcut ->
                closeInvokeSheet()
                appCatalog?.startShortcut(shortcut)
            },
            onLaunchFloating = {
                closeInvokeSheet()
                appCatalog?.launchInFloatingWindow(targetApp)
            },
            onOpenAppInfo = {
                closeInvokeSheet()
                appCatalog?.openAppInfo(targetApp)
            },
            onUninstall = {
                closeInvokeSheet()
                appCatalog?.uninstallApp(targetApp)
            },
        )
    }

    actionTargetContact?.let { targetContact ->
        val existingContactBlock = effectiveBlocks
            .filterIsInstance<CanvasBlock.ContactTile>()
            .firstOrNull { it.lookupKey == targetContact.lookupKey }
        val isInQuickPeople = invokeUiState.quickContacts.any { it.lookupKey == targetContact.lookupKey }

        QuickContactActionSheet(
            contact = targetContact,
            pinnedBlock = existingContactBlock,
            isInQuickPeople = isInQuickPeople,
            onDismiss = { actionTargetContact = null },
            onCall = { ContactsPickerHelper.dialContact(context, targetContact) },
            onMessage = { ContactsPickerHelper.messageContact(context, targetContact) },
            onPinToHome = { span ->
                canvasActions.onAddContact(targetContact, span, TileShapePreset.CIRCLE, effectiveBlocks)
            },
            onCustomizeOnHome = {
                if (existingContactBlock != null) {
                    closeInvokeSheet()
                    isEditMode = true
                    selectedBlockId = existingContactBlock.id
                }
            },
            onUnpinFromHome = {
                canvasActions.onRemoveContact(targetContact.lookupKey, effectiveBlocks)
            },
            onRemoveFromQuickPeople = {
                onRemoveQuickContact(targetContact.lookupKey)
            },
        )
    }
}

internal fun buildDefaultCanvasBlocks(installedApps: List<InstalledApp>): List<CanvasBlock> {
    val used = mutableSetOf<String>()

    fun pick(vararg keywords: String): InstalledApp? {
        val match = installedApps.firstOrNull { app ->
            app.key.packageName !in used &&
                keywords.any { kw ->
                    app.label.contains(kw, ignoreCase = true) ||
                        app.componentName.packageName.contains(kw, ignoreCase = true)
                }
        }
        val chosen = match ?: installedApps.firstOrNull { it.key.packageName !in used }
        chosen?.let { used += it.key.packageName }
        return chosen
    }

    fun placeholderKey(id: String) = AppKey("org.invokevirtual.placeholder.$id", ".Main", 0L)

    val tile1 = pick("messages", "messaging", "mail", "gmail", "chat")
    val tile2 = pick("chrome", "browser", "firefox", "search", "map")
    val tile3 = pick("camera", "photo", "gallery")
    val tile4 = pick("music", "spotify", "youtube", "calendar", "settings")

    return listOf(
        CanvasBlock.AppTile(
            id = "default_tile_1",
            appKey = tile1?.key ?: placeholderKey("messages"),
            span = TileSpan.COMPACT,
            shape = TileShapePreset.SQUIRCLE,
            tone = TileTone.PRIMARY,
        ),
        CanvasBlock.AppTile(
            id = "default_tile_2",
            appKey = tile2?.key ?: placeholderKey("browser"),
            span = TileSpan.COMPACT,
            shape = TileShapePreset.COOKIE,
            tone = TileTone.SECONDARY,
        ),
        CanvasBlock.AppTile(
            id = "default_tile_3",
            appKey = tile3?.key ?: placeholderKey("camera"),
            span = TileSpan.COMPACT,
            shape = TileShapePreset.CLOVER,
            tone = TileTone.TERTIARY,
        ),
        CanvasBlock.AppTile(
            id = "default_tile_4",
            appKey = tile4?.key ?: placeholderKey("music"),
            span = TileSpan.COMPACT,
            shape = TileShapePreset.SOFT_BURST,
            tone = TileTone.SURFACE,
        ),
    )
}

private fun fallbackMetaFor(block: CanvasBlock.AppTile, index: Int): Pair<String, ImageVector> {
    return when (block.id) {
        "default_tile_1" -> "Messages" to InvokeIcons.Chat
        "default_tile_2" -> "Browse" to InvokeIcons.Apps
        "default_tile_3" -> "Camera" to InvokeIcons.Camera
        "default_tile_4" -> "Music" to InvokeIcons.Play
        else -> when (index % 4) {
            0 -> "App" to InvokeIcons.Apps
            1 -> "Messages" to InvokeIcons.Chat
            2 -> "Spark" to InvokeIcons.Spark
            else -> "Camera" to InvokeIcons.Camera
        }
    }
}

private fun buildDockQuickActions(
    customDockKeys: List<AppKey>,
    appsByKey: Map<AppKey, InstalledApp>,
    installedApps: List<InstalledApp>,
    onLaunch: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onFallbackSearch: () -> Unit,
): List<DockQuickAction> {
    if (customDockKeys.isNotEmpty()) {
        return customDockKeys.mapIndexedNotNull { idx, key ->
            val app = appsByKey[key] ?: return@mapIndexedNotNull null
            DockQuickAction(
                id = "custom_${idx}_${key.packageName}",
                label = app.label,
                fallbackIcon = InvokeIcons.Apps,
                app = app,
                onClick = { onLaunch(app) },
                onLongClick = { onLongPressApp(app) },
            )
        }
    }

    fun find(vararg keywords: String): InstalledApp? = installedApps.firstOrNull { app ->
        keywords.any { kw ->
            app.label.contains(kw, ignoreCase = true) ||
                app.componentName.packageName.contains(kw, ignoreCase = true)
        }
    }

    val dialer = find("dialer", "phone")
    val messages = find("messaging", "messages", "mms", "whatsapp")
    val camera = find("camera")

    return listOf(
        DockQuickAction(
            id = "phone",
            label = dialer?.label ?: "Phone",
            fallbackIcon = InvokeIcons.Phone,
            app = dialer,
            onClick = { if (dialer != null) onLaunch(dialer) else onFallbackSearch() },
            onLongClick = { if (dialer != null) onLongPressApp(dialer) },
        ),
        DockQuickAction(
            id = "chat",
            label = messages?.label ?: "Messages",
            fallbackIcon = InvokeIcons.Chat,
            app = messages,
            onClick = { if (messages != null) onLaunch(messages) else onFallbackSearch() },
            onLongClick = { if (messages != null) onLongPressApp(messages) },
        ),
        DockQuickAction(
            id = "camera",
            label = camera?.label ?: "Camera",
            fallbackIcon = InvokeIcons.Camera,
            app = camera,
            onClick = { if (camera != null) onLaunch(camera) else onFallbackSearch() },
            onLongClick = { if (camera != null) onLongPressApp(camera) },
        ),
    )
}

@Preview(
    name = "Compact · Dark Everyday",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun HomeCanvasCompactDarkPreview() {
    InvokeTheme(
        settings = LauncherSettings(
            workspaceMode = WorkspaceMode.EVERYDAY,
            darkMode = DarkMode.DARK,
            dynamicColor = false,
        ),
    ) {
        HomeCanvas(
            settings = LauncherSettings(workspaceMode = WorkspaceMode.EVERYDAY),
            canvasState = HomeCanvasState(),
            installedApps = emptyList(),
            appCatalog = null,
            onSelectWorkspaceMode = {},
            onLaunchApp = { _, _ -> },
        )
    }
}

@Preview(
    name = "Compact · Light Everyday",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun HomeCanvasCompactLightPreview() {
    InvokeTheme(
        settings = LauncherSettings(
            workspaceMode = WorkspaceMode.EVERYDAY,
            darkMode = DarkMode.LIGHT,
            dynamicColor = false,
        ),
    ) {
        HomeCanvas(
            settings = LauncherSettings(workspaceMode = WorkspaceMode.EVERYDAY),
            canvasState = HomeCanvasState(),
            installedApps = emptyList(),
            appCatalog = null,
            onSelectWorkspaceMode = {},
            onLaunchApp = { _, _ -> },
        )
    }
}

@Preview(
    name = "Compact · Living Edit Mode",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun HomeCanvasEditModePreview() {
    InvokeTheme(
        settings = LauncherSettings(
            workspaceMode = WorkspaceMode.EVERYDAY,
            darkMode = DarkMode.DARK,
            dynamicColor = false,
        ),
    ) {
        HomeCanvas(
            settings = LauncherSettings(workspaceMode = WorkspaceMode.EVERYDAY),
            canvasState = HomeCanvasState(),
            installedApps = emptyList(),
            appCatalog = null,
            initialEditMode = true,
            onSelectWorkspaceMode = {},
            onLaunchApp = { _, _ -> },
        )
    }
}

@Preview(
    name = "Medium · Unfolded Foldable",
    showSystemUi = true,
    device = "spec:width=673dp,height=841dp,dpi=420",
)
@Composable
private fun HomeCanvasMediumFoldablePreview() {
    InvokeTheme(
        settings = LauncherSettings(
            workspaceMode = WorkspaceMode.EVERYDAY,
            darkMode = DarkMode.DARK,
            dynamicColor = false,
        ),
    ) {
        HomeCanvas(
            settings = LauncherSettings(workspaceMode = WorkspaceMode.EVERYDAY),
            canvasState = HomeCanvasState(),
            installedApps = emptyList(),
            appCatalog = null,
            onSelectWorkspaceMode = {},
            onLaunchApp = { _, _ -> },
        )
    }
}

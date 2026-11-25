package org.invokevirtual.launcher

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.HomeCanvasState
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.TileShapePreset
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.data.WidgetHeightTier
import org.invokevirtual.launcher.system.DefaultLauncherHelper
import org.invokevirtual.launcher.system.ScreenLockService
import org.invokevirtual.launcher.system.rememberWallpaperLegibility
import org.invokevirtual.launcher.ui.components.LocalNotificationBadges
import org.invokevirtual.launcher.ui.components.NotificationBadges
import org.invokevirtual.launcher.ui.home.HomeCanvas
import org.invokevirtual.launcher.ui.home.HomeCanvasActions
import org.invokevirtual.launcher.ui.home.buildDefaultCanvasBlocks
import org.invokevirtual.launcher.ui.invoke.InvokeViewModel
import org.invokevirtual.launcher.ui.studio.StudioActions
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme

class MainActivity : ComponentActivity() {

    private val container: InvokeApplication
        get() = application as InvokeApplication

    private var pendingPinRequest by mutableStateOf<LauncherApps.PinItemRequest?>(null)

    override fun onStart() {
        super.onStart()
        container.widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        container.widgetHost.stopListening()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractPinItemRequest(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractPinItemRequest(intent)

        setContent {
            val settings by container.settingsStore.settings.collectAsStateWithLifecycle(
                initialValue = LauncherSettings(),
            )
            val canvasState by container.homeCanvasStore.canvasState.collectAsStateWithLifecycle(
                initialValue = HomeCanvasState(),
            )
            val installedApps by container.appCatalog.apps.collectAsStateWithLifecycle()
            val badgeCounts by container.notificationBadgeStore.counts.collectAsStateWithLifecycle()
            val invokeViewModel: InvokeViewModel = viewModel(
                factory = InvokeViewModel.Factory(
                    appCatalog = container.appCatalog,
                    homeCanvasStore = container.homeCanvasStore,
                    quickContactsStore = container.quickContactsStore,
                    settingsStore = container.settingsStore,
                ),
            )
            val invokeUiState by invokeViewModel.uiState.collectAsStateWithLifecycle()
            val wallpaperLegibility = rememberWallpaperLegibility()
            val scope = rememberCoroutineScope()

            val context = LocalContext.current
            var isDefaultLauncher by remember {
                mutableStateOf(DefaultLauncherHelper.isDefaultLauncher(context))
            }
            var dismissedDefaultPrompt by rememberSaveable { mutableStateOf(false) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isDefaultLauncher = DefaultLauncherHelper.isDefaultLauncher(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val roleLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) {
                isDefaultLauncher = DefaultLauncherHelper.isDefaultLauncher(context)
            }

            val onRequestSetDefaultLauncher = remember(context, roleLauncher) {
                {
                    val roleIntent = DefaultLauncherHelper.createSetDefaultIntent(context)
                    if (roleIntent != null) {
                        roleLauncher.launch(roleIntent)
                    } else {
                        DefaultLauncherHelper.requestSetDefault(this@MainActivity)
                    }
                }
            }

            // Keep system pinned shortcuts synchronized with pinned ShortcutTiles on Home Canvas.
            LaunchedEffect(canvasState.blocks) {
                val shortcuts = canvasState.blocks.filterIsInstance<CanvasBlock.ShortcutTile>()
                if (shortcuts.isNotEmpty()) {
                    shortcuts
                        .groupBy { it.packageName to it.userSerial }
                        .forEach { (pkgAndSerial, tiles) ->
                            container.appCatalog.syncPinnedShortcuts(
                                packageName = pkgAndSerial.first,
                                shortcutIds = tiles.map { it.shortcutId },
                                userSerial = pkgAndSerial.second,
                            )
                        }
                }
            }

            val studioActions = remember(scope, container, onRequestSetDefaultLauncher) {
                StudioActions(
                    onToggleShowClock = { enabled ->
                        scope.launch { container.settingsStore.setShowClock(enabled) }
                    },
                    onSelectClockStyle = { style ->
                        scope.launch { container.settingsStore.setClockStyle(style) }
                    },
                    onSelectClockSize = { size ->
                        scope.launch { container.settingsStore.setClockSize(size) }
                    },
                    onSelectClockAlignment = { alignment ->
                        scope.launch { container.settingsStore.setClockAlignment(alignment) }
                    },
                    onSelectClockTone = { tone ->
                        scope.launch { container.settingsStore.setClockTone(tone) }
                    },
                    onPickClockCustomColorArgb = { argb ->
                        scope.launch { container.settingsStore.setClockCustomColorArgb(argb) }
                    },
                    onSelectClockShape = { shape ->
                        scope.launch { container.settingsStore.setClockShape(shape) }
                    },
                    onToggleClock24Hour = { enabled ->
                        scope.launch { container.settingsStore.setClock24Hour(enabled) }
                    },
                    onToggleClockSeconds = { enabled ->
                        scope.launch { container.settingsStore.setShowClockSeconds(enabled) }
                    },
                    onToggleClockAmPm = { enabled ->
                        scope.launch { container.settingsStore.setShowClockAmPm(enabled) }
                    },
                    onToggleClockDate = { enabled ->
                        scope.launch { container.settingsStore.setShowClockDate(enabled) }
                    },
                    onToggleClockModeBadge = { enabled ->
                        scope.launch { container.settingsStore.setShowClockModeBadge(enabled) }
                    },
                    onToggleGlanceBattery = { enabled ->
                        scope.launch { container.settingsStore.setShowGlanceBattery(enabled) }
                    },
                    onToggleGlanceAlarm = { enabled ->
                        scope.launch { container.settingsStore.setShowGlanceAlarm(enabled) }
                    },
                    onSelectFontPreset = { preset ->
                        scope.launch { container.settingsStore.setFontPreset(preset) }
                    },
                    onSelectDarkMode = { mode ->
                        scope.launch { container.settingsStore.setDarkMode(mode) }
                    },
                    onToggleAmoledBlack = { enabled ->
                        scope.launch { container.settingsStore.setAmoledBlack(enabled) }
                    },
                    onToggleDynamicColor = { enabled ->
                        scope.launch { container.settingsStore.setDynamicColor(enabled) }
                    },
                    onSelectPalette = { palette ->
                        scope.launch { container.settingsStore.setPalette(palette) }
                    },
                    onPickCustomSeedArgb = { argb ->
                        scope.launch { container.settingsStore.setCustomSeedColor(argb) }
                    },
                    onSelectIconStyle = { style ->
                        scope.launch { container.settingsStore.setIconStyle(style) }
                    },
                    onToggleShowLabels = { enabled ->
                        scope.launch { container.settingsStore.setShowLabels(enabled) }
                    },
                    onToggleExpressiveMotion = { enabled ->
                        scope.launch { container.settingsStore.setExpressiveMotion(enabled) }
                    },
                    onToggleShowDock = { enabled ->
                        scope.launch { container.settingsStore.setShowDock(enabled) }
                    },
                    onSelectDockStyle = { style ->
                        scope.launch { container.settingsStore.setDockStyle(style) }
                    },
                    onToggleShowDockModeButton = { enabled ->
                        scope.launch { container.settingsStore.setShowDockModeButton(enabled) }
                    },
                    onSetDockApps = { apps ->
                        scope.launch { container.settingsStore.setDockApps(apps) }
                    },
                    onSetDrawerTransparency = { transparency ->
                        scope.launch { container.settingsStore.setDrawerTransparency(transparency) }
                    },
                    onToggleShowQuickPeople = { enabled ->
                        scope.launch { container.settingsStore.setShowQuickPeople(enabled) }
                    },
                    onToggleBottomSearchBar = { enabled ->
                        scope.launch { container.settingsStore.setBottomSearchBar(enabled) }
                    },
                    onToggleDoubleTapToSleep = { enabled ->
                        scope.launch { container.settingsStore.setDoubleTapToSleep(enabled) }
                        if (!enabled) ScreenLockService.disable()
                    },
                    onSelectBadgeStyle = { style ->
                        scope.launch { container.settingsStore.setBadgeStyle(style) }
                    },
                    onRequestSetDefaultLauncher = onRequestSetDefaultLauncher,
                )
            }

            val canvasActions = remember(scope, container) {
                HomeCanvasActions(
                    onAddApp = { app, span, shape, customCols, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addAppTile(
                                appKey = app.key,
                                fallbackDefaults = fallbackDefaults,
                                span = span,
                                shape = shape,
                                customColumns = customCols,
                                customHeightDp = customHeightDp,
                            )
                        }
                    },
                    onAddShortcut = { shortcut, span, shape, customCols, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addShortcutTile(
                                packageName = shortcut.packageName,
                                shortcutId = shortcut.shortcutId,
                                userSerial = shortcut.userSerial,
                                label = shortcut.label,
                                fallbackDefaults = fallbackDefaults,
                                span = span,
                                shape = shape,
                                customColumns = customCols,
                                customHeightDp = customHeightDp,
                            )
                        }
                    },
                    onRemoveShortcut = { pkg, id, serial, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.removeShortcut(
                                packageName = pkg,
                                shortcutId = id,
                                userSerial = serial,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onAddContact = { contact, span, shape, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addContactTile(
                                contact = contact,
                                fallbackDefaults = fallbackDefaults,
                                span = span,
                                shape = shape,
                            )
                        }
                    },
                    onRemoveContact = { lookupKey, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.removeContactTile(
                                lookupKey = lookupKey,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onAddSpacer = { spacerId, span, customCols, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addSpacerBlock(
                                fallbackDefaults = fallbackDefaults,
                                span = span,
                                customColumns = customCols,
                                customHeightDp = customHeightDp,
                                explicitId = spacerId,
                            )
                        }
                    },
                    onAddFolder = { title, apps, span, shape, customCols, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addFolderTile(
                                title = title,
                                apps = apps,
                                fallbackDefaults = fallbackDefaults,
                                span = span,
                                shape = shape,
                                customColumns = customCols,
                                customHeightDp = customHeightDp,
                            )
                        }
                    },
                    onUpdateFolder = { folderId, title, apps, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.updateFolder(
                                folderId = folderId,
                                title = title,
                                apps = apps,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onAddAppToFolder = { folderId, appKey, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addAppToFolder(
                                folderId = folderId,
                                appKey = appKey,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onRemoveAppFromFolder = { folderId, appKey, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.removeAppFromFolder(
                                folderId = folderId,
                                appKey = appKey,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderLayout = { folderId, layoutMode, gridColumns, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderLayout(
                                folderId = folderId,
                                layoutMode = layoutMode,
                                gridColumns = gridColumns,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onAddSpacerToFolder = { folderId, spacerId, span, customCols, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addSpacerToFolder(
                                folderId = folderId,
                                span = span,
                                customColumns = customCols,
                                customHeightDp = customHeightDp,
                                explicitId = spacerId,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onRemoveFolderItem = { folderId, itemId, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.removeFolderItem(
                                folderId = folderId,
                                itemId = itemId,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onMoveFolderItem = { folderId, itemId, delta, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.moveFolderItem(
                                folderId = folderId,
                                itemId = itemId,
                                delta = delta,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderItemSpan = { folderId, itemId, span, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderItemSpan(
                                folderId = folderId,
                                itemId = itemId,
                                span = span,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderItemCustomSize = { folderId, itemId, columns, heightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderItemCustomSize(
                                folderId = folderId,
                                itemId = itemId,
                                columns = columns,
                                heightDp = heightDp,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderItemShape = { folderId, itemId, shape, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderItemShape(
                                folderId = folderId,
                                itemId = itemId,
                                shape = shape,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderItemTone = { folderId, itemId, tone, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderItemTone(
                                folderId = folderId,
                                itemId = itemId,
                                tone = tone,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetFolderItemCustomBgColor = { folderId, itemId, argb, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setFolderItemCustomBgColor(
                                folderId = folderId,
                                itemId = itemId,
                                argb = argb,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onAddWidget = { appWidgetId, heightTier, shape, customColumns, customHeightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.addWidgetBlock(
                                appWidgetId = appWidgetId,
                                fallbackDefaults = fallbackDefaults,
                                heightTier = heightTier,
                                shape = shape,
                                customColumns = customColumns,
                                customHeightDp = customHeightDp,
                            )
                        }
                    },
                    onRemoveBlock = { block, fallbackDefaults ->
                        if (block is CanvasBlock.WidgetBlock) {
                            container.widgetHost.deleteWidgetId(block.appWidgetId)
                        }
                        scope.launch {
                            container.homeCanvasStore.removeBlock(
                                blockId = block.id,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onMoveBlock = { blockId, delta, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.moveBlock(
                                blockId = blockId,
                                delta = delta,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetTileSpan = { blockId, span, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setBlockSpan(
                                blockId = blockId,
                                span = span,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetTileCustomSize = { blockId, columns, heightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setTileCustomSize(
                                blockId = blockId,
                                columns = columns,
                                heightDp = heightDp,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetWidgetSize = { blockId, tier, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setWidgetSize(
                                blockId = blockId,
                                tier = tier,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetWidgetCustomSize = { blockId, columns, heightDp, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setWidgetCustomSize(
                                blockId = blockId,
                                columns = columns,
                                heightDp = heightDp,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetShape = { blockId, shape, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setBlockShape(
                                blockId = blockId,
                                shape = shape,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetTone = { blockId, tone, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setBlockTone(
                                blockId = blockId,
                                tone = tone,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onSetCustomBgColor = { blockId, argb, fallbackDefaults ->
                        scope.launch {
                            container.homeCanvasStore.setBlockCustomBgColor(
                                blockId = blockId,
                                argb = argb,
                                fallbackDefaults = fallbackDefaults,
                            )
                        }
                    },
                    onResetDefaults = {
                        scope.launch { container.homeCanvasStore.resetDefaults() }
                    },
                )
            }

            InvokeTheme(
                settings = settings,
                preferDarkWallpaperBars = settings.effectiveAmoledBlack ||
                    !wallpaperLegibility.supportsDarkText,
            ) {
                CompositionLocalProvider(
                    LocalNotificationBadges provides NotificationBadges(settings.badgeStyle, badgeCounts),
                ) {
                    HomeCanvas(
                        settings = settings,
                        canvasState = canvasState,
                        installedApps = installedApps,
                        canvasActions = canvasActions,
                        invokeUiState = invokeUiState,
                        studioActions = studioActions,
                        widgetHost = container.widgetHost,
                        appCatalog = container.appCatalog,
                        onSelectWorkspaceMode = { mode ->
                            scope.launch { container.settingsStore.setWorkspaceMode(mode) }
                        },
                        onLaunchApp = { app, bounds ->
                            container.appCatalog.launch(
                                app = app,
                                sourceBounds = bounds,
                                sourceView = window?.decorView,
                            )
                        },
                        onLaunchShortcut = { pkg, shortcutId, userSerial, bounds ->
                            container.appCatalog.startShortcutById(
                                packageName = pkg,
                                shortcutId = shortcutId,
                                userSerial = userSerial,
                                sourceBounds = bounds,
                                sourceView = window?.decorView,
                            )
                        },
                        onInvokeQueryChange = invokeViewModel::onQueryChange,
                        onInvokeClearQuery = invokeViewModel::clearQuery,
                        onInvokeSelectFilter = invokeViewModel::selectFilter,
                        onTogglePrivateSpace = invokeViewModel::togglePrivateSpace,
                        onSavePickedContacts = invokeViewModel::savePickedContacts,
                        onRemoveQuickContact = invokeViewModel::removeQuickContact,
                        onTogglePinInDrawer = { appKey ->
                            scope.launch { container.settingsStore.togglePinnedDrawerApp(appKey) }
                        },
                    )

                    if (!isDefaultLauncher && !dismissedDefaultPrompt) {
                        SetDefaultLauncherDialog(
                            onDismiss = { dismissedDefaultPrompt = true },
                            onConfirm = {
                                dismissedDefaultPrompt = true
                                onRequestSetDefaultLauncher()
                            },
                        )
                    }

                    pendingPinRequest?.takeIf { it.isValid }?.let { request ->
                        PinItemConfirmDialog(
                            request = request,
                            onDismiss = { pendingPinRequest = null },
                            onConfirm = {
                                pendingPinRequest = null
                                val fallbackBlocks = if (canvasState.isCustomized) {
                                    canvasState.blocks
                                } else {
                                    buildDefaultCanvasBlocks(installedApps)
                                }
                                when (request.requestType) {
                                    LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                                        val info = request.shortcutInfo
                                        if (info != null && request.accept()) {
                                            val serial = container.appCatalog.getUserSerial(info.userHandle)
                                            val label = info.shortLabel?.toString()?.takeIf { it.isNotBlank() }
                                                ?: info.longLabel?.toString()?.takeIf { it.isNotBlank() }
                                                ?: info.id
                                            scope.launch {
                                                container.homeCanvasStore.addShortcutTile(
                                                    packageName = info.`package`,
                                                    shortcutId = info.id,
                                                    userSerial = serial,
                                                    label = label,
                                                    fallbackDefaults = fallbackBlocks,
                                                    span = TileSpan.COMPACT,
                                                    shape = TileShapePreset.LEAF,
                                                )
                                            }
                                        }
                                    }

                                    LauncherApps.PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                                        val info = request.getAppWidgetProviderInfo(this@MainActivity)
                                        if (info != null) {
                                            val widgetId = container.widgetHost.allocateWidgetId()
                                            val extras = Bundle().apply {
                                                putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                                            }
                                            if (container.widgetHost.tryBindWidget(widgetId, info) &&
                                                request.accept(extras)
                                            ) {
                                                scope.launch {
                                                    container.homeCanvasStore.addWidgetBlock(
                                                        appWidgetId = widgetId,
                                                        fallbackDefaults = fallbackBlocks,
                                                        heightTier = WidgetHeightTier.MEDIUM,
                                                        shape = TileShapePreset.SQUIRCLE,
                                                    )
                                                }
                                            } else {
                                                container.widgetHost.deleteWidgetId(widgetId)
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun extractPinItemRequest(intent: Intent?) {
        val action = intent?.action ?: return
        if (action != LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT &&
            action != LauncherApps.ACTION_CONFIRM_PIN_APPWIDGET
        ) {
            return
        }
        val launcherApps = getSystemService(LauncherApps::class.java) ?: return
        val request = runCatching { launcherApps.getPinItemRequest(intent) }.getOrNull() ?: return
        if (request.isValid) {
            pendingPinRequest = request
        }
    }
}

@androidx.compose.runtime.Composable
private fun PinItemConfirmDialog(
    request: LauncherApps.PinItemRequest,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val isShortcut = request.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT
    val itemTitle = if (isShortcut) {
        val info = request.shortcutInfo
        info?.shortLabel?.toString()?.takeIf { it.isNotBlank() }
            ?: info?.longLabel?.toString()?.takeIf { it.isNotBlank() }
            ?: "Shortcut"
    } else {
        "Widget"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isShortcut) "Add shortcut to Home?" else "Add widget to Home?",
                style = MaterialTheme.typography.headlineSmallEmphasized,
            )
        },
        text = {
            Text(
                text = if (isShortcut) {
                    "\"$itemTitle\" will appear on your Living Home Canvas."
                } else {
                    "Pin this widget directly onto your Living Home Canvas."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Add to Home")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@androidx.compose.runtime.Composable
private fun SetDefaultLauncherDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = InvokeIcons.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        },
        title = {
            Text(
                text = "Set as default launcher",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(
                text = "Set Invoke as your default home app to use gesture navigation, the living canvas, and unified search every time you go home.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Set as default")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Not now")
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

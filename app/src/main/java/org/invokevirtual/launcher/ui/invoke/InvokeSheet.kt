package org.invokevirtual.launcher.ui.invoke

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.invokevirtual.launcher.data.CanvasBlock
import org.invokevirtual.launcher.data.DarkMode
import org.invokevirtual.launcher.data.HomeCanvasState
import org.invokevirtual.launcher.data.IconStyle
import org.invokevirtual.launcher.data.LauncherSettings
import org.invokevirtual.launcher.data.QuickContact
import org.invokevirtual.launcher.data.TileSpan
import org.invokevirtual.launcher.system.AppCatalog
import org.invokevirtual.launcher.system.AppIconAsset
import org.invokevirtual.launcher.system.AppKey
import org.invokevirtual.launcher.system.AppProfile
import org.invokevirtual.launcher.system.AppShortcutItem
import org.invokevirtual.launcher.system.ContactsPickerHelper
import org.invokevirtual.launcher.system.InstalledApp
import org.invokevirtual.launcher.system.PrivateSpaceState
import org.invokevirtual.launcher.ui.components.NotificationBadgeBox
import org.invokevirtual.launcher.ui.components.TileIconGlyph
import org.invokevirtual.launcher.ui.theme.InvokeIcons
import org.invokevirtual.launcher.ui.theme.InvokeTheme
import org.invokevirtual.launcher.ui.theme.LocalReducedMotion
import org.invokevirtual.launcher.ui.theme.rememberMorphShape
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Unified M3 Expressive 1.5.0 Invoke Sheet:
 * - Instant fuzzy search (prefix, initials, word-boundary, subsequence) with Enter-to-launch
 * - Inline quick calculator card with tap-to-copy
 * - Android 17 zero-permission Quick People (1-tap Call & Message)
 * - Live verb-first app shortcuts across Personal & Work profiles
 * - Authentic M3E connected [FilledTonalToggleButton] filter strip (`All`, `Pinned`, `Work`)
 * - Authentic M3E [SegmentedListItem] A–Z groups with haptic scrubber rail + morphing letter bubble
 * - Android 15–17 Private Space vault anchored cleanly at the bottom
 */
@Composable
fun InvokeSheet(
    uiState: InvokeUiState,
    autoFocusSearch: Boolean,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelectFilter: (InvokeFilter) -> Unit,
    onDismiss: () -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onTogglePrivateSpace: (Boolean) -> Unit,
    onSavePickedContacts: (List<QuickContact>) -> Unit,
    onLongPressQuickContact: (QuickContact) -> Unit = {},
    showQuickPeople: Boolean = true,
    drawerTransparency: Float = 0f,
    bottomSearchBar: Boolean = false,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val clampedTransparency = drawerTransparency.coerceIn(0f, 1f)
    val sheetAlpha = 1f - clampedTransparency
    val baseScrimColor = BottomSheetDefaults.ScrimColor

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = sheetAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = baseScrimColor.copy(alpha = baseScrimColor.alpha * (1f - clampedTransparency)),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        InvokeSheetContent(
            uiState = uiState,
            autoFocusSearch = autoFocusSearch,
            iconStyle = iconStyle,
            showQuickPeople = showQuickPeople,
            drawerTransparency = clampedTransparency,
            bottomSearchBar = bottomSearchBar,
            appCatalog = appCatalog,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            onSelectFilter = onSelectFilter,
            onLaunchApp = onLaunchApp,
            onLongPressApp = onLongPressApp,
            onLaunchShortcut = onLaunchShortcut,
            onTogglePrivateSpace = onTogglePrivateSpace,
            onSavePickedContacts = onSavePickedContacts,
            onLongPressQuickContact = onLongPressQuickContact,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
                .imePadding(),
        )
    }
}

@Composable
internal fun InvokeSheetContent(
    uiState: InvokeUiState,
    autoFocusSearch: Boolean,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelectFilter: (InvokeFilter) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit,
    onLaunchShortcut: (AppShortcutItem) -> Unit,
    onTogglePrivateSpace: (Boolean) -> Unit,
    onSavePickedContacts: (List<QuickContact>) -> Unit,
    modifier: Modifier = Modifier,
    onLongPressQuickContact: (QuickContact) -> Unit = {},
    showQuickPeople: Boolean = true,
    drawerTransparency: Float = 0f,
    bottomSearchBar: Boolean = false,
) {
    val cardAlpha = (1f - drawerTransparency.coerceIn(0f, 1f) * 0.65f).coerceIn(0.35f, 1f)
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val supportsAndroid17Picker = remember(context, inPreview) {
        !inPreview && ContactsPickerHelper.isAndroid17PickerSupported(context)
    }

    val contactsPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val sessionUri = result.data?.data ?: return@rememberLauncherForActivityResult
            coroutineScope.launch {
                val picked = ContactsPickerHelper.parsePickerResult(context, sessionUri)
                if (picked.isNotEmpty()) {
                    onSavePickedContacts(picked)
                }
            }
        }
    }

    val launchContactsPicker: () -> Unit = {
        if (!inPreview) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            runCatching {
                contactsPickerLauncher.launch(ContactsPickerHelper.createPickerIntent(context))
            }
        }
    }

    LaunchedEffect(autoFocusSearch) {
        listState.scrollToItem(0)
        if (autoFocusSearch && !inPreview) {
            delay(80.milliseconds)
            runCatching { focusRequester.requestFocus() }
        }
    }

    val showQuickPeopleInBrowse = showQuickPeople &&
        !uiState.isSearching &&
        uiState.activeFilter == InvokeFilter.ALL
    val browseQuickPeopleItemCount = when {
        !showQuickPeopleInBrowse -> 0
        uiState.quickContacts.isEmpty() -> 1
        else -> 1 + uiState.quickContacts.size
    }
    val showPinnedSectionInBrowse = !uiState.isSearching &&
            uiState.activeFilter == InvokeFilter.ALL &&
            uiState.pinnedDrawerApps.isNotEmpty()
    val browsePinnedItemCount = if (showPinnedSectionInBrowse) {
        1 + uiState.pinnedDrawerApps.size
    } else {
        0
    }

    val sectionStartIndices = remember(
        uiState.sections,
        browseQuickPeopleItemCount,
        browsePinnedItemCount,
    ) {
        buildMap {
            var itemIndex = browseQuickPeopleItemCount + browsePinnedItemCount
            for (section in uiState.sections) {
                put(section.letter, itemIndex)
                itemIndex += 1 + section.apps.size
            }
        }
    }

    val scrubberLetters = remember(uiState.sections) {
        uiState.sections.map { it.letter }
    }
    val isPinnedFilterBrowse = !uiState.isSearching && uiState.activeFilter == InvokeFilter.PINNED
    val showScrubber = !uiState.isSearching && !isPinnedFilterBrowse && scrubberLetters.size >= 3

    var activeScrubLetter by remember { mutableStateOf<Char?>(null) }
    var scrubTouchY by remember { mutableFloatStateOf(0f) }
    val searchFieldState = rememberTextFieldState(initialText = uiState.query)

    LaunchedEffect(searchFieldState) {
        snapshotFlow { searchFieldState.text.toString() }.collect { text ->
            onQueryChange(text)
        }
    }

    LaunchedEffect(uiState.query, uiState.activeFilter) {
        listState.scrollToItem(0)
    }

    val reverseSearchLayout = bottomSearchBar && uiState.isSearching

    val searchBar: @Composable () -> Unit = {
        OutlinedTextField(
            state = searchFieldState,
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = if (bottomSearchBar) 12.dp else 0.dp,
                )
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "Invoke apps, people, shortcuts, or math…",
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
            trailingIcon = {
                if (searchFieldState.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchFieldState.clearText()
                            onClearQuery()
                        },
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        Icon(
                            imageVector = InvokeIcons.Close,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = {
                uiState.topMatch?.let(onLaunchApp)
            },
            shape = RoundedCornerShape(percent = 50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
            ),
        )
    }

    val filterStrip: @Composable () -> Unit = {
        InvokeFilterStrip(
            activeFilter = uiState.activeFilter,
            showWorkFilter = uiState.hasWorkApps,
            cardAlpha = cardAlpha,
            onSelectFilter = { filter ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectFilter(filter)
            },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!bottomSearchBar) {
            searchBar()
            filterStrip()
        }

        Box(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                state = listState,
                reverseLayout = reverseSearchLayout,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = if (showScrubber) 32.dp else 20.dp,
                    top = if (bottomSearchBar) 8.dp else 0.dp,
                    bottom = if (bottomSearchBar) 4.dp else 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(
                    ListItemDefaults.SegmentedGap,
                    if (bottomSearchBar) Alignment.Bottom else Alignment.Top,
                ),
            ) {
                if (uiState.isSearching) {
                    val hasMath = uiState.mathResult != null
                    val hasContacts = showQuickPeople && uiState.matchingContacts.isNotEmpty()
                    val hasContactPrompt =
                        showQuickPeople && uiState.showContactPickerPromptInSearch
                    val hasShortcuts = uiState.matchingShortcuts.isNotEmpty()
                    val hasApps = uiState.rankedSearchResults.isNotEmpty()

                    if (reverseSearchLayout) {
                        uiState.mathResult?.let { math ->
                            item(key = "math_result") {
                                InlineMathCard(
                                    result = math,
                                    cardAlpha = cardAlpha,
                                    modifier = Modifier.padding(
                                        top = if (hasApps || hasShortcuts || hasContacts || hasContactPrompt) 10.dp else 0.dp,
                                    ),
                                )
                            }
                        }

                        if (hasApps) {
                            itemsIndexed(
                                items = uiState.rankedSearchResults,
                                key = { _, app ->
                                    "search_${app.key.packageName}/${app.key.className}/${app.key.userSerial}"
                                },
                            ) { index, app ->
                                InvokeAppRow(
                                    app = app,
                                    isPinnedInDrawer = app.key in uiState.pinnedDrawerKeys,
                                    isTopSearchMatch = index == 0,
                                    index = uiState.rankedSearchResults.size - 1 - index,
                                    count = uiState.rankedSearchResults.size,
                                    cardAlpha = cardAlpha,
                                    iconStyle = iconStyle,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                            if (hasMath || hasShortcuts || hasContacts || hasContactPrompt) {
                                item(key = "apps_search_header") {
                                    SectionHeaderLabel(
                                        title = "Apps",
                                        count = uiState.rankedSearchResults.size,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                                    )
                                }
                            }
                        }

                        if (hasShortcuts) {
                            if (hasApps || hasMath) {
                                item(key = "shortcuts_spacer") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            itemsIndexed(
                                items = uiState.matchingShortcuts,
                                key = { _, shortcut -> "shortcut_${shortcut.id}" },
                            ) { index, shortcut ->
                                ShortcutResultRow(
                                    shortcut = shortcut,
                                    index = uiState.matchingShortcuts.size - 1 - index,
                                    count = uiState.matchingShortcuts.size,
                                    cardAlpha = cardAlpha,
                                    onClick = { onLaunchShortcut(shortcut) },
                                )
                            }
                            item(key = "shortcuts_header") {
                                SectionHeaderLabel(
                                    title = "Actions & Shortcuts",
                                    count = uiState.matchingShortcuts.size,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                                )
                            }
                        }

                        if (hasContacts) {
                            if (hasApps || hasShortcuts || hasMath) {
                                item(key = "people_search_spacer") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            itemsIndexed(
                                items = uiState.matchingContacts,
                                key = { _, contact -> "search_contact_${contact.lookupKey}" },
                            ) { index, contact ->
                                QuickContactRow(
                                    contact = contact,
                                    index = uiState.matchingContacts.size - 1 - index,
                                    count = uiState.matchingContacts.size,
                                    isPinned = uiState.homeCanvas.containsContact(contact.lookupKey),
                                    cardAlpha = cardAlpha,
                                    onCall = { ContactsPickerHelper.dialContact(context, contact) },
                                    onMessage = {
                                        ContactsPickerHelper.messageContact(
                                            context,
                                            contact
                                        )
                                    },
                                    onLongClick = { onLongPressQuickContact(contact) },
                                )
                            }
                            item(key = "people_search_header") {
                                QuickPeopleHeaderRow(
                                    count = uiState.matchingContacts.size,
                                    onAddClick = launchContactsPicker,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                                )
                            }
                        } else if (hasContactPrompt) {
                            item(key = "people_search_picker_prompt") {
                                QuickPeoplePickerBanner(
                                    subtitle = if (supportsAndroid17Picker) {
                                        "Android 17 Contacts Picker · no permission needed"
                                    } else {
                                        "Pick favorite contacts for 1-tap call & text"
                                    },
                                    cardAlpha = cardAlpha,
                                    onPickClick = launchContactsPicker,
                                    modifier = Modifier.padding(
                                        bottom = if (hasApps || hasShortcuts || hasMath) 10.dp else 0.dp,
                                    ),
                                )
                            }
                        }

                        if (!hasApps && !hasMath && !hasShortcuts && !hasContacts && !hasContactPrompt) {
                            item(key = "empty_search") {
                                EmptySearchCard(
                                    query = uiState.query.trim(),
                                    cardAlpha = cardAlpha,
                                )
                            }
                        }
                    } else {
                        uiState.mathResult?.let { math ->
                            item(key = "math_result") {
                                InlineMathCard(
                                    result = math,
                                    cardAlpha = cardAlpha,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                        }

                        if (hasContacts) {
                            item(key = "people_search_header") {
                                QuickPeopleHeaderRow(
                                    count = uiState.matchingContacts.size,
                                    onAddClick = launchContactsPicker,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = uiState.matchingContacts,
                                key = { _, contact -> "search_contact_${contact.lookupKey}" },
                            ) { index, contact ->
                                QuickContactRow(
                                    contact = contact,
                                    index = index,
                                    count = uiState.matchingContacts.size,
                                    isPinned = uiState.homeCanvas.containsContact(contact.lookupKey),
                                    cardAlpha = cardAlpha,
                                    onCall = { ContactsPickerHelper.dialContact(context, contact) },
                                    onMessage = {
                                        ContactsPickerHelper.messageContact(
                                            context,
                                            contact
                                        )
                                    },
                                    onLongClick = { onLongPressQuickContact(contact) },
                                )
                            }
                            item(key = "people_search_spacer") {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        } else if (hasContactPrompt) {
                            item(key = "people_search_picker_prompt") {
                                QuickPeoplePickerBanner(
                                    subtitle = if (supportsAndroid17Picker) {
                                        "Android 17 Contacts Picker · no permission needed"
                                    } else {
                                        "Pick favorite contacts for 1-tap call & text"
                                    },
                                    cardAlpha = cardAlpha,
                                    onPickClick = launchContactsPicker,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                        }

                        if (hasShortcuts) {
                            item(key = "shortcuts_header") {
                                SectionHeaderLabel(
                                    title = "Actions & Shortcuts",
                                    count = uiState.matchingShortcuts.size,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = uiState.matchingShortcuts,
                                key = { _, shortcut -> "shortcut_${shortcut.id}" },
                            ) { index, shortcut ->
                                ShortcutResultRow(
                                    shortcut = shortcut,
                                    index = index,
                                    count = uiState.matchingShortcuts.size,
                                    cardAlpha = cardAlpha,
                                    onClick = { onLaunchShortcut(shortcut) },
                                )
                            }
                            item(key = "shortcuts_spacer") {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        if (hasApps) {
                            if (hasMath || hasShortcuts || hasContacts || hasContactPrompt) {
                                item(key = "apps_search_header") {
                                    SectionHeaderLabel(
                                        title = "Apps",
                                        count = uiState.rankedSearchResults.size,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                                    )
                                }
                            }
                            itemsIndexed(
                                items = uiState.rankedSearchResults,
                                key = { _, app ->
                                    "search_${app.key.packageName}/${app.key.className}/${app.key.userSerial}"
                                },
                            ) { index, app ->
                                InvokeAppRow(
                                    app = app,
                                    isPinnedInDrawer = app.key in uiState.pinnedDrawerKeys,
                                    isTopSearchMatch = index == 0,
                                    index = index,
                                    count = uiState.rankedSearchResults.size,
                                    cardAlpha = cardAlpha,
                                    iconStyle = iconStyle,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                        } else if (!hasMath && !hasShortcuts && !hasContacts && !hasContactPrompt) {
                            item(key = "empty_search") {
                                EmptySearchCard(
                                    query = uiState.query.trim(),
                                    cardAlpha = cardAlpha,
                                )
                            }
                        }
                    }
                } else {
                    if (showQuickPeopleInBrowse) {
                        if (uiState.quickContacts.isEmpty()) {
                            item(key = "quick_people_banner") {
                                QuickPeoplePickerBanner(
                                    subtitle = if (supportsAndroid17Picker) {
                                        "Android 17 Contacts Picker · zero permissions"
                                    } else {
                                        "Pick favorite people for 1-tap call & text"
                                    },
                                    cardAlpha = cardAlpha,
                                    onPickClick = launchContactsPicker,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                        } else {
                            item(key = "quick_people_header") {
                                QuickPeopleHeaderRow(
                                    count = uiState.quickContacts.size,
                                    onAddClick = launchContactsPicker,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = uiState.quickContacts,
                                key = { _, contact -> "browse_contact_${contact.lookupKey}" },
                            ) { index, contact ->
                                QuickContactRow(
                                    contact = contact,
                                    index = index,
                                    count = uiState.quickContacts.size,
                                    isPinned = uiState.homeCanvas.containsContact(contact.lookupKey),
                                    cardAlpha = cardAlpha,
                                    onCall = { ContactsPickerHelper.dialContact(context, contact) },
                                    onMessage = { ContactsPickerHelper.messageContact(context, contact) },
                                    onLongClick = { onLongPressQuickContact(contact) },
                                )
                            }
                        }
                    }

                    if (isPinnedFilterBrowse) {
                        if (uiState.pinnedDrawerApps.isEmpty()) {
                            item(key = "empty_pinned") {
                                EmptyPinnedCard(cardAlpha = cardAlpha)
                            }
                        } else {
                            item(key = "filter_pinned_header") {
                                SectionHeaderLabel(
                                    title = "Pinned",
                                    count = uiState.pinnedDrawerApps.size,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = uiState.pinnedDrawerApps,
                                key = { _, app ->
                                    "filter_pinned_${app.key.packageName}/${app.key.className}/${app.key.userSerial}"
                                },
                            ) { index, app ->
                                InvokeAppRow(
                                    app = app,
                                    isPinnedInDrawer = true,
                                    isTopSearchMatch = false,
                                    index = index,
                                    count = uiState.pinnedDrawerApps.size,
                                    cardAlpha = cardAlpha,
                                    iconStyle = iconStyle,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                        }
                    } else {
                        if (showPinnedSectionInBrowse) {
                            item(key = "pinned_drawer_header") {
                                SectionHeaderLabel(
                                    title = "Pinned",
                                    count = uiState.pinnedDrawerApps.size,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = uiState.pinnedDrawerApps,
                                key = { _, app ->
                                    "pinned_${app.key.packageName}/${app.key.className}/${app.key.userSerial}"
                                },
                            ) { index, app ->
                                InvokeAppRow(
                                    app = app,
                                    isPinnedInDrawer = true,
                                    isTopSearchMatch = false,
                                    index = index,
                                    count = uiState.pinnedDrawerApps.size,
                                    cardAlpha = cardAlpha,
                                    iconStyle = iconStyle,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                        }

                        uiState.sections.forEach { section ->
                            item(key = "header_${section.letter}") {
                                LetterSectionHeader(
                                    letter = section.letter,
                                    count = section.apps.size,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                                )
                            }
                            itemsIndexed(
                                items = section.apps,
                                key = { _, app ->
                                    "${app.key.packageName}/${app.key.className}/${app.key.userSerial}"
                                },
                            ) { index, app ->
                                InvokeAppRow(
                                    app = app,
                                    isPinnedInDrawer = app.key in uiState.pinnedDrawerKeys,
                                    isTopSearchMatch = false,
                                    index = index,
                                    count = section.apps.size,
                                    cardAlpha = cardAlpha,
                                    iconStyle = iconStyle,
                                    appCatalog = appCatalog,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                )
                            }
                        }
                    }

                    if (uiState.privateSpace != PrivateSpaceState.Unavailable) {
                        item(key = "private_space_vault") {
                            PrivateSpaceCard(
                                state = uiState.privateSpace,
                                iconStyle = iconStyle,
                                appCatalog = appCatalog,
                                cardAlpha = cardAlpha,
                                onSetLocked = onTogglePrivateSpace,
                                onLaunchApp = onLaunchApp,
                                onLongPressApp = onLongPressApp,
                                modifier = Modifier.padding(top = 18.dp),
                            )
                        }
                    }
                }
            }

            if (showScrubber) {
                AlphabetScrubberRail(
                    letters = scrubberLetters,
                    activeLetter = activeScrubLetter,
                    onScrubLetter = { letter, yPos ->
                        scrubTouchY = yPos
                        if (activeScrubLetter != letter) {
                            activeScrubLetter = letter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            sectionStartIndices[letter]?.let { targetIdx ->
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIdx)
                                }
                            }
                        }
                    },
                    onScrubEnd = { activeScrubLetter = null },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp),
                )

                ScrubberMorphBubble(
                    letter = activeScrubLetter,
                    touchY = scrubTouchY,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 30.dp),
                )
            }
        }

        if (bottomSearchBar) {
            filterStrip()
            searchBar()
        }
    }
}

@Composable
private fun InvokeFilterStrip(
    activeFilter: InvokeFilter,
    showWorkFilter: Boolean,
    cardAlpha: Float,
    onSelectFilter: (InvokeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = remember(showWorkFilter) {
        if (showWorkFilter) {
            listOf(InvokeFilter.ALL, InvokeFilter.PINNED, InvokeFilter.WORK)
        } else {
            listOf(InvokeFilter.ALL, InvokeFilter.PINNED)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        filters.forEachIndexed { index, filter ->
            val isSelected = activeFilter == filter
            val shapes = when {
                filters.size == 1 -> FilledTonalToggleButtonDefaults.shapesFor(
                    FilledTonalToggleButtonDefaults.size,
                )
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            FilledTonalToggleButton(
                checked = isSelected,
                onCheckedChange = { if (it) onSelectFilter(filter) },
                shapes = shapes,
                colors = FilledTonalToggleButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = cardAlpha),
                    checkedContainerColor = MaterialTheme.colorScheme.secondary.copy(
                        alpha = cardAlpha.coerceAtLeast(0.6f),
                    ),
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
            ) {
                if (filter == InvokeFilter.WORK) {
                    Icon(
                        imageVector = InvokeIcons.Work,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = filter.label,
                    style = if (isSelected) {
                        MaterialTheme.typography.titleSmallEmphasized
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun InlineMathCard(
    result: MathEvalResult,
    cardAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var copied by remember(result.formattedValue) { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1_400.milliseconds)
            copied = false
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = cardAlpha.coerceAtLeast(0.45f)),
        modifier = modifier.fillMaxWidth(),
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = InvokeIcons.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${result.expression} =",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = result.formattedValue,
                    style = MaterialTheme.typography.headlineMediumEmphasized.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            FilledTonalButton(
                onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(
                        ClipData.newPlainText("Invoke Calculation", result.formattedValue),
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    copied = true
                },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Icon(
                    imageVector = InvokeIcons.Copy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (copied) "Copied" else "Copy",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ShortcutResultRow(
    shortcut: AppShortcutItem,
    index: Int,
    count: Int,
    cardAlpha: Float,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (shortcut.icon != null) {
                    Image(
                        bitmap = shortcut.icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                } else {
                    Icon(
                        imageVector = InvokeIcons.ShortcutBolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = shortcut.appLabel,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        content = {
            Text(
                text = shortcut.label,
                style = MaterialTheme.typography.titleMediumEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun InvokeAppRow(
    app: InstalledApp,
    isPinnedInDrawer: Boolean,
    isTopSearchMatch: Boolean,
    index: Int,
    count: Int,
    cardAlpha: Float,
    iconStyle: IconStyle,
    appCatalog: AppCatalog?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val iconAsset by produceState<AppIconAsset?>(initialValue = null, app, appCatalog) {
        value = appCatalog?.loadIcon(app)
    }

    SegmentedListItem(
        onClick = onClick,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongClick()
        },
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 74.dp)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("App actions for ${app.label}") {
                        onLongClick()
                        true
                    },
                )
            },
        leadingContent = {
            NotificationBadgeBox(apps = listOf(app.key), iconSize = 48.dp) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    TileIconGlyph(
                        iconAsset = iconAsset,
                        fallbackGlyph = InvokeIcons.Apps,
                        iconStyle = iconStyle,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        },
        supportingContent = if (app.isWorkProfile) {
            {
                Text(
                    text = "Work profile",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
        } else {
            null
        },
        trailingContent = when {
            isTopSearchMatch -> {
                {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "Enter",
                            style = MaterialTheme.typography.labelSmallEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                        )
                    }
                }
            }
            isPinnedInDrawer -> {
                {
                    Icon(
                        imageVector = InvokeIcons.Pin,
                        contentDescription = "Pinned in drawer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            else -> null
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

@Composable
private fun LetterSectionHeader(
    letter: Char,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionHeaderLabel(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptySearchCard(
    query: String,
    cardAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = "No matches for \"$query\"",
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Try initials (like yt) or quick math (145 * 1.18)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyPinnedCard(
    cardAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = cardAlpha),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = InvokeIcons.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = "No pinned apps yet",
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Long-press any app in the drawer and tap Pin in drawer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AlphabetScrubberRail(
    letters: List<Char>,
    activeLetter: Char?,
    onScrubLetter: (Char, Float) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var railHeightPx by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .width(28.dp)
            .onSizeChanged { railHeightPx = it.height.coerceAtLeast(1) }
            .pointerInput(letters) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    fun dispatchForY(y: Float) {
                        val fraction = (y / railHeightPx.toFloat()).coerceIn(0f, 0.9999f)
                        val idx = (fraction * letters.size).toInt().coerceIn(0, letters.lastIndex)
                        onScrubLetter(letters[idx], y.coerceIn(0f, railHeightPx.toFloat()))
                    }

                    dispatchForY(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            break
                        }
                        dispatchForY(change.position.y)
                        change.consume()
                    }
                    onScrubEnd()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            letters.forEach { letter ->
                val isActive = letter == activeLetter
                Text(
                    text = letter.toString(),
                    style = if (isActive) {
                        MaterialTheme.typography.labelSmallEmphasized
                    } else {
                        MaterialTheme.typography.labelSmall
                    },
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ScrubberMorphBubble(
    letter: Char?,
    touchY: Float,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val bubbleSizePx = with(density) { 64.dp.toPx() }
    val morphProgress by animateFloatAsState(
        targetValue = if (letter != null) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "scrubberBubbleMorph",
    )
    val bubbleShape = rememberMorphShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Cookie4Sided,
        progress = if (reducedMotion) 0f else morphProgress,
    )

    AnimatedVisibility(
        visible = letter != null,
        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) +
            scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = 0.72f),
        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) +
            scaleOut(MaterialTheme.motionScheme.fastSpatialSpec(), targetScale = 0.72f),
        modifier = modifier.offset {
            IntOffset(
                x = 0,
                y = (touchY - bubbleSizePx / 2f).roundToInt().coerceAtLeast(0),
            )
        },
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(bubbleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter?.toString().orEmpty(),
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

private fun previewApp(
    label: String,
    pkg: String,
    profile: AppProfile = AppProfile.PERSONAL,
): InstalledApp = InstalledApp(
    key = AppKey(pkg, "$pkg.MainActivity", 0L),
    label = label,
    componentName = ComponentName(pkg, "$pkg.MainActivity"),
    userHandle = Process.myUserHandle(),
    profile = profile,
)

@Preview(
    name = "Invoke Sheet · Browse & Private Space",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun InvokeSheetBrowsePreview() {
    val sampleApps = remember {
        listOf(
            previewApp("Calendar", "org.sample.calendar"),
            previewApp("Camera", "org.sample.camera"),
            previewApp("Messages", "org.sample.messages"),
            previewApp("Slack", "org.sample.slack", AppProfile.WORK),
            previewApp("Spotify", "org.sample.spotify"),
        )
    }
    val canvas = remember(sampleApps) {
        HomeCanvasState(
            blocks = listOf(
                CanvasBlock.AppTile("p1", sampleApps[4].key, TileSpan.HERO),
                CanvasBlock.AppTile("p2", sampleApps[2].key, TileSpan.COMPACT),
            ),
            isCustomized = true,
        )
    }
    val state = remember(sampleApps, canvas) {
        InvokeUiState(
            query = "",
            activeFilter = InvokeFilter.ALL,
            hasWorkApps = true,
            isSearching = false,
            sections = InvokeSearchEngine.groupAppsByLetter(sampleApps),
            pinnedDrawerApps = listOf(sampleApps[4], sampleApps[2]),
            pinnedDrawerKeys = setOf(sampleApps[4].key, sampleApps[2].key),
            homeCanvas = canvas,
            privateSpace = PrivateSpaceState.Locked(Process.myUserHandle()),
        )
    }

    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxSize(),
        ) {
            InvokeSheetContent(
                uiState = state,
                autoFocusSearch = false,
                iconStyle = IconStyle.THEMED_MONOCHROME,
                appCatalog = null,
                onQueryChange = {},
                onClearQuery = {},
                onSelectFilter = {},
                onLaunchApp = {},
                onLongPressApp = {},
                onLaunchShortcut = {},
                onTogglePrivateSpace = {},
                onSavePickedContacts = {},
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp),
            )
        }
    }
}

@Preview(
    name = "Invoke Sheet · Fuzzy Search & Quick Calc",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun InvokeSheetSearchPreview() {
    val sampleApps = remember {
        listOf(
            previewApp("YouTube", "org.sample.youtube"),
            previewApp("YouTube Music", "org.sample.ytmusic"),
        )
    }
    val sampleContacts = remember {
        listOf(
            QuickContact("k1", "Maya Lin", "+1 555-0142"),
        )
    }
    val state = remember(sampleApps, sampleContacts) {
        InvokeUiState(
            query = "145 * 1.18",
            activeFilter = InvokeFilter.ALL,
            hasWorkApps = false,
            isSearching = true,
            rankedSearchResults = sampleApps,
            matchingContacts = sampleContacts,
            mathResult = InvokeSearchEngine.tryEvaluateMath("145 * 1.18"),
        )
    }

    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxSize(),
        ) {
            InvokeSheetContent(
                uiState = state,
                autoFocusSearch = false,
                iconStyle = IconStyle.THEMED_MONOCHROME,
                appCatalog = null,
                onQueryChange = {},
                onClearQuery = {},
                onSelectFilter = {},
                onLaunchApp = {},
                onLongPressApp = {},
                onLaunchShortcut = {},
                onTogglePrivateSpace = {},
                onSavePickedContacts = {},
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp),
            )
        }
    }
}

@Preview(
    name = "Invoke Sheet · Bottom Search (Few Results)",
    showSystemUi = true,
    device = "spec:width=411dp,height=915dp,dpi=420",
)
@Composable
private fun InvokeSheetBottomSearchPreview() {
    val sampleApps = remember {
        listOf(
            previewApp("YouTube", "org.sample.youtube"),
            previewApp("YouTube Music", "org.sample.ytmusic"),
        )
    }
    val state = remember(sampleApps) {
        InvokeUiState(
            query = "yt",
            activeFilter = InvokeFilter.ALL,
            hasWorkApps = false,
            isSearching = true,
            rankedSearchResults = sampleApps,
        )
    }

    InvokeTheme(settings = LauncherSettings(darkMode = DarkMode.DARK, dynamicColor = false)) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxSize(),
        ) {
            InvokeSheetContent(
                uiState = state,
                autoFocusSearch = false,
                iconStyle = IconStyle.THEMED_MONOCHROME,
                bottomSearchBar = true,
                appCatalog = null,
                onQueryChange = {},
                onClearQuery = {},
                onSelectFilter = {},
                onLaunchApp = {},
                onLongPressApp = {},
                onLaunchShortcut = {},
                onTogglePrivateSpace = {},
                onSavePickedContacts = {},
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(top = 12.dp),
            )
        }
    }
}

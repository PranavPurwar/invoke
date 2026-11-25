package org.invokevirtual.launcher.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.invokevirtual.launcher.system.AppKey

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "invoke_launcher_settings",
)

enum class WorkspaceMode(val label: String) {
    EVERYDAY("Everyday"),
    DEEP_FOCUS("Deep Focus"),
    NIGHT_AMOLED("Night OLED"),
}

enum class DarkMode(val label: String) {
    SYSTEM("Auto"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class ClockStyle(val label: String) {
    EDITORIAL_STACKED("Stacked"),
    KINETIC_SPLIT("Kinetic"),
    SHAPE_DIAL("Dial"),
    MINIMAL_INLINE("Inline"),
}

enum class ClockSize(val label: String, val scale: Float) {
    COMPACT("Compact", 0.78f),
    STANDARD("Standard", 1f),
    LARGE("Large", 1.18f),
}

enum class ClockAlignment(val label: String) {
    START("Left"),
    CENTER("Center"),
}

enum class DockStyle(val label: String) {
    TOOLBAR("Toolbar"),
    MINIMAL_SEARCH("Search Pill"),
}

enum class BadgeStyle(val label: String) {
    OFF("Off"),
    DOT("Dot"),
    COUNT("Count"),
}

enum class LauncherFontPreset(
    val displayName: String,
    val clockFontName: String,
    val headlineFontName: String,
    val bodyFontName: String,
    val pairingSummary: String,
    val clockIsSerif: Boolean = false,
) {
    GOOGLE_SANS_FLEX(
        displayName = "Flex Pair",
        clockFontName = "Google Sans Flex",
        headlineFontName = "Bricolage Grotesque",
        bodyFontName = "Plus Jakarta Sans",
        pairingSummary = "Google Sans Flex · Bricolage · Jakarta",
    ),
    BRICOLAGE(
        displayName = "Editorial",
        clockFontName = "Fraunces",
        headlineFontName = "Bricolage Grotesque",
        bodyFontName = "Plus Jakarta Sans",
        pairingSummary = "Fraunces · Bricolage · Jakarta",
        clockIsSerif = true,
    ),
    PLUS_JAKARTA(
        displayName = "Geometric",
        clockFontName = "Space Grotesk",
        headlineFontName = "Plus Jakarta Sans",
        bodyFontName = "Roboto Flex",
        pairingSummary = "Space Grotesk · Jakarta · Roboto Flex",
    ),
    ROBOTO_FLEX(
        displayName = "Studio",
        clockFontName = "Roboto Flex",
        headlineFontName = "Space Grotesk",
        bodyFontName = "Plus Jakarta Sans",
        pairingSummary = "Roboto Flex · Space Grotesk · Jakarta",
    ),
}

enum class IconStyle(val label: String) {
    THEMED_MONOCHROME("Monochrome"),
    FULL_COLOR("Full Color"),
}

enum class ExpressivePalette(val displayName: String) {
    TANGERINE("Tangerine"),
    SAGE("Sage"),
    COBALT("Cobalt"),
    PLUM("Plum"),
}

@Immutable
data class LauncherSettings(
    val workspaceMode: WorkspaceMode = WorkspaceMode.EVERYDAY,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val palette: ExpressivePalette = ExpressivePalette.TANGERINE,
    val customSeedArgb: Int? = null,
    val amoledBlack: Boolean = false,
    val expressiveMotion: Boolean = true,
    val showClock: Boolean = true,
    val clockStyle: ClockStyle = ClockStyle.EDITORIAL_STACKED,
    val clockSize: ClockSize = ClockSize.STANDARD,
    val clockAlignment: ClockAlignment = ClockAlignment.START,
    val clockTone: TileTone = TileTone.PRIMARY,
    val clockCustomColorArgb: Int? = null,
    val clockShape: TileShapePreset = TileShapePreset.COOKIE,
    val clock24Hour: Boolean = true,
    val showClockSeconds: Boolean = false,
    val showClockAmPm: Boolean = false,
    val showClockDate: Boolean = true,
    val showClockModeBadge: Boolean = false,
    val showGlanceBattery: Boolean = true,
    val showGlanceAlarm: Boolean = true,
    val fontPreset: LauncherFontPreset = LauncherFontPreset.BRICOLAGE,
    val iconStyle: IconStyle = IconStyle.THEMED_MONOCHROME,
    val showLabels: Boolean = true,
    val showDock: Boolean = true,
    val dockStyle: DockStyle = DockStyle.TOOLBAR,
    val showDockModeButton: Boolean = true,
    val dockApps: List<AppKey> = emptyList(),
    val pinnedDrawerApps: List<AppKey> = emptyList(),
    val drawerTransparency: Float = 0f,
    val showQuickPeople: Boolean = true,
    val bottomSearchBar: Boolean = false,
    val doubleTapToSleep: Boolean = false,
    val badgeStyle: BadgeStyle = BadgeStyle.OFF,
) {
    /** Night OLED mode forces true-black dark surfaces without mutating the user's daytime preference. */
    val effectiveAmoledBlack: Boolean
        get() = amoledBlack || workspaceMode == WorkspaceMode.NIGHT_AMOLED

    val effectiveDarkMode: DarkMode
        get() = if (workspaceMode == WorkspaceMode.NIGHT_AMOLED) DarkMode.DARK else darkMode
}

class SettingsStore(private val context: Context) {
    private object Keys {
        val WORKSPACE_MODE = stringPreferencesKey("workspace_mode")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PALETTE = stringPreferencesKey("palette")
        val CUSTOM_SEED_ARGB = intPreferencesKey("custom_seed_argb")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val EXPRESSIVE_MOTION = booleanPreferencesKey("expressive_motion")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val CLOCK_SIZE = stringPreferencesKey("clock_size")
        val CLOCK_ALIGNMENT = stringPreferencesKey("clock_alignment")
        val CLOCK_TONE = stringPreferencesKey("clock_tone")
        val CLOCK_CUSTOM_COLOR_ARGB = intPreferencesKey("clock_custom_color_argb")
        val CLOCK_SHAPE = stringPreferencesKey("clock_shape")
        val CLOCK_24_HOUR = booleanPreferencesKey("clock_24_hour")
        val SHOW_CLOCK_SECONDS = booleanPreferencesKey("show_clock_seconds")
        val SHOW_CLOCK_AM_PM = booleanPreferencesKey("show_clock_am_pm")
        val SHOW_CLOCK_DATE = booleanPreferencesKey("show_clock_date")
        val SHOW_CLOCK_MODE_BADGE = booleanPreferencesKey("show_clock_mode_badge")
        val SHOW_GLANCE_BATTERY = booleanPreferencesKey("show_glance_battery")
        val SHOW_GLANCE_ALARM = booleanPreferencesKey("show_glance_alarm")
        val FONT_PRESET = stringPreferencesKey("font_preset")
        val ICON_STYLE = stringPreferencesKey("icon_style")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SHOW_DOCK = booleanPreferencesKey("show_dock")
        val DOCK_STYLE = stringPreferencesKey("dock_style")
        val SHOW_DOCK_MODE_BUTTON = booleanPreferencesKey("show_dock_mode_button")
        val DOCK_APPS = stringPreferencesKey("dock_apps")
        val PINNED_DRAWER_APPS = stringPreferencesKey("pinned_drawer_apps")
        val DRAWER_TRANSPARENCY = floatPreferencesKey("drawer_transparency")
        val SHOW_QUICK_PEOPLE = booleanPreferencesKey("show_quick_people")
        val BOTTOM_SEARCH_BAR = booleanPreferencesKey("bottom_search_bar")
        val DOUBLE_TAP_TO_SLEEP = booleanPreferencesKey("double_tap_to_sleep")
        val BADGE_STYLE = stringPreferencesKey("badge_style")
    }

    val settings: Flow<LauncherSettings> = context.launcherDataStore.data.map { prefs ->
        LauncherSettings(
            workspaceMode = prefs[Keys.WORKSPACE_MODE]
                ?.let { runCatching { WorkspaceMode.valueOf(it) }.getOrNull() }
                ?: WorkspaceMode.EVERYDAY,
            darkMode = prefs[Keys.DARK_MODE]
                ?.let { runCatching { DarkMode.valueOf(it) }.getOrNull() }
                ?: DarkMode.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            palette = prefs[Keys.PALETTE]
                ?.let { runCatching { ExpressivePalette.valueOf(it) }.getOrNull() }
                ?: ExpressivePalette.TANGERINE,
            customSeedArgb = prefs[Keys.CUSTOM_SEED_ARGB],
            amoledBlack = prefs[Keys.AMOLED_BLACK] ?: false,
            expressiveMotion = prefs[Keys.EXPRESSIVE_MOTION] ?: true,
            showClock = prefs[Keys.SHOW_CLOCK] ?: true,
            clockStyle = prefs[Keys.CLOCK_STYLE]
                ?.let { runCatching { ClockStyle.valueOf(it) }.getOrNull() }
                ?: ClockStyle.EDITORIAL_STACKED,
            clockSize = prefs[Keys.CLOCK_SIZE]
                ?.let { runCatching { ClockSize.valueOf(it) }.getOrNull() }
                ?: ClockSize.STANDARD,
            clockAlignment = prefs[Keys.CLOCK_ALIGNMENT]
                ?.let { runCatching { ClockAlignment.valueOf(it) }.getOrNull() }
                ?: ClockAlignment.START,
            clockTone = prefs[Keys.CLOCK_TONE]
                ?.let { runCatching { TileTone.valueOf(it) }.getOrNull() }
                ?: TileTone.PRIMARY,
            clockCustomColorArgb = prefs[Keys.CLOCK_CUSTOM_COLOR_ARGB],
            clockShape = prefs[Keys.CLOCK_SHAPE]
                ?.let { runCatching { TileShapePreset.valueOf(it) }.getOrNull() }
                ?: TileShapePreset.COOKIE,
            clock24Hour = prefs[Keys.CLOCK_24_HOUR] ?: false,
            showClockSeconds = prefs[Keys.SHOW_CLOCK_SECONDS] ?: false,
            showClockAmPm = prefs[Keys.SHOW_CLOCK_AM_PM] ?: false,
            showClockDate = prefs[Keys.SHOW_CLOCK_DATE] ?: true,
            showClockModeBadge = prefs[Keys.SHOW_CLOCK_MODE_BADGE] ?: true,
            showGlanceBattery = prefs[Keys.SHOW_GLANCE_BATTERY] ?: true,
            showGlanceAlarm = prefs[Keys.SHOW_GLANCE_ALARM] ?: true,
            fontPreset = prefs[Keys.FONT_PRESET]
                ?.let { runCatching { LauncherFontPreset.valueOf(it) }.getOrNull() }
                ?: LauncherFontPreset.GOOGLE_SANS_FLEX,
            iconStyle = prefs[Keys.ICON_STYLE]
                ?.let { runCatching { IconStyle.valueOf(it) }.getOrNull() }
                ?: IconStyle.THEMED_MONOCHROME,
            showLabels = prefs[Keys.SHOW_LABELS] ?: true,
            showDock = prefs[Keys.SHOW_DOCK] ?: true,
            dockStyle = prefs[Keys.DOCK_STYLE]
                ?.let { runCatching { DockStyle.valueOf(it) }.getOrNull() }
                ?: DockStyle.TOOLBAR,
            showDockModeButton = prefs[Keys.SHOW_DOCK_MODE_BUTTON] ?: true,
            dockApps = decodeDockApps(prefs[Keys.DOCK_APPS].orEmpty()),
            pinnedDrawerApps = decodeAppKeys(prefs[Keys.PINNED_DRAWER_APPS].orEmpty()),
            drawerTransparency = (prefs[Keys.DRAWER_TRANSPARENCY] ?: 0f).coerceIn(0f, 1f),
            showQuickPeople = prefs[Keys.SHOW_QUICK_PEOPLE] ?: true,
            bottomSearchBar = prefs[Keys.BOTTOM_SEARCH_BAR] ?: false,
            doubleTapToSleep = prefs[Keys.DOUBLE_TAP_TO_SLEEP] ?: false,
            badgeStyle = prefs[Keys.BADGE_STYLE]
                ?.let { runCatching { BadgeStyle.valueOf(it) }.getOrNull() }
                ?: BadgeStyle.OFF,
        )
    }

    suspend fun setWorkspaceMode(mode: WorkspaceMode) {
        context.launcherDataStore.edit { it[Keys.WORKSPACE_MODE] = mode.name }
    }

    suspend fun setDarkMode(mode: DarkMode) {
        context.launcherDataStore.edit { it[Keys.DARK_MODE] = mode.name }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.AMOLED_BLACK] = enabled }
    }

    suspend fun setShowClock(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_CLOCK] = enabled }
    }

    suspend fun setClockStyle(style: ClockStyle) {
        context.launcherDataStore.edit { it[Keys.CLOCK_STYLE] = style.name }
    }

    suspend fun setClockSize(size: ClockSize) {
        context.launcherDataStore.edit { it[Keys.CLOCK_SIZE] = size.name }
    }

    suspend fun setClockAlignment(alignment: ClockAlignment) {
        context.launcherDataStore.edit { it[Keys.CLOCK_ALIGNMENT] = alignment.name }
    }

    suspend fun setClockTone(tone: TileTone) {
        context.launcherDataStore.edit {
            it[Keys.CLOCK_TONE] = tone.name
            it.remove(Keys.CLOCK_CUSTOM_COLOR_ARGB)
        }
    }

    suspend fun setClockCustomColorArgb(argb: Int?) {
        context.launcherDataStore.edit {
            if (argb != null) {
                it[Keys.CLOCK_CUSTOM_COLOR_ARGB] = argb or (0xFF shl 24)
            } else {
                it.remove(Keys.CLOCK_CUSTOM_COLOR_ARGB)
            }
        }
    }

    suspend fun setClockShape(shape: TileShapePreset) {
        context.launcherDataStore.edit { it[Keys.CLOCK_SHAPE] = shape.name }
    }

    suspend fun setClock24Hour(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.CLOCK_24_HOUR] = enabled }
    }

    suspend fun setShowClockSeconds(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_CLOCK_SECONDS] = enabled }
    }

    suspend fun setShowClockAmPm(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_CLOCK_AM_PM] = enabled }
    }

    suspend fun setShowClockDate(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_CLOCK_DATE] = enabled }
    }

    suspend fun setShowClockModeBadge(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_CLOCK_MODE_BADGE] = enabled }
    }

    suspend fun setShowGlanceBattery(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_GLANCE_BATTERY] = enabled }
    }

    suspend fun setShowGlanceAlarm(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_GLANCE_ALARM] = enabled }
    }

    suspend fun setFontPreset(preset: LauncherFontPreset) {
        context.launcherDataStore.edit { it[Keys.FONT_PRESET] = preset.name }
    }

    suspend fun setIconStyle(style: IconStyle) {
        context.launcherDataStore.edit { it[Keys.ICON_STYLE] = style.name }
    }

    suspend fun setShowLabels(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_LABELS] = enabled }
    }

    suspend fun setExpressiveMotion(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.EXPRESSIVE_MOTION] = enabled }
    }

    suspend fun setShowDock(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_DOCK] = enabled }
    }

    suspend fun setDockStyle(style: DockStyle) {
        context.launcherDataStore.edit { it[Keys.DOCK_STYLE] = style.name }
    }

    suspend fun setShowDockModeButton(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.SHOW_DOCK_MODE_BUTTON] = enabled }
    }

    suspend fun setDockApps(apps: List<AppKey>) {
        context.launcherDataStore.edit {
            val cleaned = apps.distinct().take(MAX_DOCK_APPS)
            if (cleaned.isEmpty()) {
                it.remove(Keys.DOCK_APPS)
            } else {
                it[Keys.DOCK_APPS] = encodeDockApps(cleaned)
            }
        }
    }

    suspend fun togglePinnedDrawerApp(appKey: AppKey) {
        context.launcherDataStore.edit { prefs ->
            val current = decodeAppKeys(prefs[Keys.PINNED_DRAWER_APPS].orEmpty())
            val updated = if (appKey in current) {
                current - appKey
            } else {
                current + appKey
            }
            if (updated.isEmpty()) {
                prefs.remove(Keys.PINNED_DRAWER_APPS)
            } else {
                prefs[Keys.PINNED_DRAWER_APPS] = encodeAppKeys(updated)
            }
        }
    }

    suspend fun setDrawerTransparency(transparency: Float) {
        context.launcherDataStore.edit {
            it[Keys.DRAWER_TRANSPARENCY] = transparency.coerceIn(0f, 1f)
        }
    }

    suspend fun setShowQuickPeople(enabled: Boolean) {
        context.launcherDataStore.edit {
            it[Keys.SHOW_QUICK_PEOPLE] = enabled
        }
    }

    suspend fun setBottomSearchBar(enabled: Boolean) {
        context.launcherDataStore.edit {
            it[Keys.BOTTOM_SEARCH_BAR] = enabled
        }
    }

    suspend fun setDoubleTapToSleep(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.DOUBLE_TAP_TO_SLEEP] = enabled }
    }

    suspend fun setBadgeStyle(style: BadgeStyle) {
        context.launcherDataStore.edit { it[Keys.BADGE_STYLE] = style.name }
    }

    suspend fun setPalette(palette: ExpressivePalette) {
        context.launcherDataStore.edit {
            it[Keys.PALETTE] = palette.name
            it[Keys.DYNAMIC_COLOR] = false
            it.remove(Keys.CUSTOM_SEED_ARGB)
        }
    }

    suspend fun setCustomSeedColor(argb: Int?) {
        context.launcherDataStore.edit {
            if (argb != null) {
                it[Keys.CUSTOM_SEED_ARGB] = argb or (0xFF shl 24)
                it[Keys.DYNAMIC_COLOR] = false
            } else {
                it.remove(Keys.CUSTOM_SEED_ARGB)
            }
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.launcherDataStore.edit {
            it[Keys.DYNAMIC_COLOR] = enabled
            if (enabled) {
                it.remove(Keys.CUSTOM_SEED_ARGB)
            }
        }
    }

    companion object {
        const val MAX_DOCK_APPS = 4
        private const val RECORD_SEP = '\u001E'
        private const val FIELD_SEP = '\u001F'

        internal fun encodeAppKeys(apps: List<AppKey>): String =
            apps.distinct().joinToString(RECORD_SEP.toString()) { key ->
                "${key.packageName}$FIELD_SEP${key.className}$FIELD_SEP${key.userSerial}"
            }

        internal fun decodeAppKeys(raw: String): List<AppKey> {
            if (raw.isEmpty()) return emptyList()
            return raw.split(RECORD_SEP).mapNotNull { record ->
                val parts = record.split(FIELD_SEP)
                if (parts.size < 3) return@mapNotNull null
                val serial = parts[2].toLongOrNull() ?: return@mapNotNull null
                AppKey(parts[0], parts[1], serial)
            }.distinct()
        }

        internal fun encodeDockApps(apps: List<AppKey>): String =
            encodeAppKeys(apps.take(MAX_DOCK_APPS))

        internal fun decodeDockApps(raw: String): List<AppKey> =
            decodeAppKeys(raw).take(MAX_DOCK_APPS)
    }
}

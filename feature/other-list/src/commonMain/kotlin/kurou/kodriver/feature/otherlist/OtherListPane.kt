package kurou.kodriver.feature.otherlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ScrollToTopEffect
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.item_access_local_network_permission
import kurou.kodriver.feature.otherlist.generated.resources.item_console_ip
import kurou.kodriver.feature.otherlist.generated.resources.item_debug_state
import kurou.kodriver.feature.otherlist.generated.resources.item_dynamic_color
import kurou.kodriver.feature.otherlist.generated.resources.item_feedback
import kurou.kodriver.feature.otherlist.generated.resources.item_github_repository
import kurou.kodriver.feature.otherlist.generated.resources.item_github_repository_star_request
import kurou.kodriver.feature.otherlist.generated.resources.item_haptic_feedback
import kurou.kodriver.feature.otherlist.generated.resources.item_keep_screen_on
import kurou.kodriver.feature.otherlist.generated.resources.item_license
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_background_opacity
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_text_size
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_visible
import kurou.kodriver.feature.otherlist.generated.resources.item_readout_start_sound
import kurou.kodriver.feature.otherlist.generated.resources.item_release_page
import kurou.kodriver.feature.otherlist.generated.resources.item_server_ip
import kurou.kodriver.feature.otherlist.generated.resources.item_startup
import kurou.kodriver.feature.otherlist.generated.resources.item_theme
import kurou.kodriver.feature.otherlist.generated.resources.item_tts_engine_missing
import kurou.kodriver.feature.otherlist.generated.resources.item_tts_language_data_missing
import kurou.kodriver.feature.otherlist.generated.resources.item_voice
import kurou.kodriver.feature.otherlist.generated.resources.item_volume
import kurou.kodriver.feature.otherlist.generated.resources.item_volume_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_windows_speech_unavailable
import kurou.kodriver.feature.otherlist.generated.resources.readout_start_sound_electronic_noise
import kurou.kodriver.feature.otherlist.generated.resources.readout_start_sound_formula_radio
import kurou.kodriver.feature.otherlist.generated.resources.section_app_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_connection_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_information
import kurou.kodriver.feature.otherlist.generated.resources.section_overlay_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_readout_settings
import kurou.kodriver.feature.otherlist.generated.resources.theme_dark
import kurou.kodriver.feature.otherlist.generated.resources.theme_light
import kurou.kodriver.feature.otherlist.generated.resources.theme_system
import kurou.kodriver.feature.otherlist.generated.resources.voice_system_default
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private enum class OtherListSection {
    ConnectionSettings,
    ReadoutSettings,
    OverlaySettings,
    AppSettings,
    Information,
}

private val otherListSections =
    listOf(
        OtherListSection.ConnectionSettings,
        OtherListSection.ReadoutSettings,
        OtherListSection.OverlaySettings,
        OtherListSection.AppSettings,
        OtherListSection.Information,
    )

private fun OtherListItemType.section(): OtherListSection =
    when (this) {
        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        -> OtherListSection.ConnectionSettings

        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> OtherListSection.ReadoutSettings

        OtherListItemType.OverlayVisible,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayBackgroundOpacity,
        -> OtherListSection.OverlaySettings

        OtherListItemType.KeepScreenOn,
        OtherListItemType.Theme,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> OtherListSection.AppSettings

        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        -> OtherListSection.Information

        OtherListItemType.DebugState -> OtherListSection.Information
    }

@Composable
private fun otherItemDisplayName(itemType: OtherListItemType): String =
    when (itemType) {
        OtherListItemType.AccessLocalNetworkPermission -> {
            stringResource(Res.string.item_access_local_network_permission)
        }

        OtherListItemType.ServerIp -> {
            stringResource(Res.string.item_server_ip)
        }

        OtherListItemType.ConsoleIp -> {
            stringResource(Res.string.item_console_ip)
        }

        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> {
            otherReadoutSettingsItemDisplayName(itemType)
        }

        OtherListItemType.OverlayVisible,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayBackgroundOpacity,
        -> {
            otherOverlaySettingsItemDisplayName(itemType)
        }

        OtherListItemType.GitHubRepository -> {
            stringResource(Res.string.item_github_repository)
        }

        OtherListItemType.ReleasePage -> {
            stringResource(Res.string.item_release_page)
        }

        OtherListItemType.Feedback -> {
            stringResource(Res.string.item_feedback)
        }

        OtherListItemType.License -> {
            stringResource(Res.string.item_license)
        }

        OtherListItemType.DebugState -> {
            stringResource(Res.string.item_debug_state)
        }

        OtherListItemType.KeepScreenOn,
        OtherListItemType.Theme,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> {
            otherAppSettingsItemDisplayName(itemType)
        }
    }

@Composable
private fun otherReadoutSettingsItemDisplayName(itemType: OtherListItemType): String =
    when (itemType) {
        OtherListItemType.Volume -> stringResource(Res.string.item_volume)
        OtherListItemType.Voice -> stringResource(Res.string.item_voice)
        OtherListItemType.ReadoutStartSound -> stringResource(Res.string.item_readout_start_sound)
        OtherListItemType.TtsEngineMissing -> stringResource(Res.string.item_tts_engine_missing)
        OtherListItemType.TtsLanguageDataMissing -> stringResource(Res.string.item_tts_language_data_missing)
        OtherListItemType.WindowsSpeechUnavailable -> stringResource(Res.string.item_windows_speech_unavailable)
        else -> error("unexpected item type: $itemType")
    }

@Composable
private fun otherAppSettingsItemDisplayName(itemType: OtherListItemType): String =
    when (itemType) {
        OtherListItemType.KeepScreenOn -> stringResource(Res.string.item_keep_screen_on)
        OtherListItemType.Theme -> stringResource(Res.string.item_theme)
        OtherListItemType.DynamicColor -> stringResource(Res.string.item_dynamic_color)
        OtherListItemType.HapticFeedback -> stringResource(Res.string.item_haptic_feedback)
        OtherListItemType.Startup -> stringResource(Res.string.item_startup)
        else -> error("unexpected item type: $itemType")
    }

@Composable
private fun otherOverlaySettingsItemDisplayName(itemType: OtherListItemType): String =
    when (itemType) {
        OtherListItemType.OverlayVisible -> stringResource(Res.string.item_overlay_visible)
        OtherListItemType.OverlayTextSize -> stringResource(Res.string.item_overlay_text_size)
        OtherListItemType.OverlayBackgroundOpacity -> stringResource(Res.string.item_overlay_background_opacity)
        else -> error("unexpected item type: $itemType")
    }

@Composable
private fun otherListSectionTitle(section: OtherListSection): String =
    when (section) {
        OtherListSection.ConnectionSettings -> stringResource(Res.string.section_connection_settings)
        OtherListSection.ReadoutSettings -> stringResource(Res.string.section_readout_settings)
        OtherListSection.OverlaySettings -> stringResource(Res.string.section_overlay_settings)
        OtherListSection.AppSettings -> stringResource(Res.string.section_app_settings)
        OtherListSection.Information -> stringResource(Res.string.section_information)
    }

private fun otherListItemLeadingIconVector(itemType: OtherListItemType): ImageVector =
    when (itemType) {
        OtherListItemType.AccessLocalNetworkPermission -> Icons.Outlined.Wifi

        OtherListItemType.ServerIp -> Icons.Outlined.Computer

        OtherListItemType.ConsoleIp -> Icons.Outlined.SportsEsports

        OtherListItemType.Volume -> Icons.AutoMirrored.Outlined.VolumeUp

        OtherListItemType.ReadoutStartSound -> Icons.Outlined.MusicNote

        OtherListItemType.Voice,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> Icons.Outlined.RecordVoiceOver

        OtherListItemType.OverlayVisible,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayBackgroundOpacity,
        -> otherOverlaySettingsItemLeadingIconVector(itemType)

        OtherListItemType.KeepScreenOn,
        OtherListItemType.Theme,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> otherAppSettingsItemLeadingIconVector(itemType)

        OtherListItemType.GitHubRepository -> Icons.Outlined.Star

        OtherListItemType.ReleasePage -> Icons.Outlined.NewReleases

        OtherListItemType.Feedback -> Icons.Outlined.Feedback

        OtherListItemType.License -> Icons.Outlined.Description

        OtherListItemType.DebugState -> Icons.Outlined.Code
    }

private fun otherOverlaySettingsItemLeadingIconVector(itemType: OtherListItemType): ImageVector =
    when (itemType) {
        OtherListItemType.OverlayVisible -> Icons.Outlined.Layers
        OtherListItemType.OverlayTextSize -> Icons.Outlined.FormatSize
        OtherListItemType.OverlayBackgroundOpacity -> Icons.Outlined.Opacity
        else -> error("unexpected item type: $itemType")
    }

private fun otherAppSettingsItemLeadingIconVector(itemType: OtherListItemType): ImageVector =
    when (itemType) {
        OtherListItemType.KeepScreenOn -> Icons.Outlined.BedtimeOff
        OtherListItemType.Theme -> Icons.Outlined.BrightnessHigh
        OtherListItemType.DynamicColor -> Icons.Outlined.Palette
        OtherListItemType.HapticFeedback -> Icons.Outlined.Vibration
        OtherListItemType.Startup -> Icons.Outlined.PowerSettingsNew
        else -> error("unexpected item type: $itemType")
    }

@Composable
private fun OtherListItemLeadingIcon(
    itemType: OtherListItemType,
    hasAppUpdate: Boolean,
    accessLocalNetworkPermissionGranted: Boolean,
) {
    val imageVector = otherListItemLeadingIconVector(itemType)
    val showBadge =
        when (itemType) {
            OtherListItemType.ReleasePage -> hasAppUpdate
            OtherListItemType.AccessLocalNetworkPermission -> !accessLocalNetworkPermissionGranted
            else -> false
        }
    if (itemType == OtherListItemType.ReleasePage || itemType == OtherListItemType.AccessLocalNetworkPermission) {
        BadgedBox(badge = { if (showBadge) Badge() }) {
            Icon(imageVector = imageVector, contentDescription = null)
        }
    } else {
        Icon(imageVector = imageVector, contentDescription = null)
    }
}

@Composable
private fun OtherListItemTrailingIcon(itemType: OtherListItemType) {
    when (itemType) {
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)

        OtherListItemType.ReadoutStartSound,
        OtherListItemType.Theme,
        OtherListItemType.OverlayTextSize,
        -> Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)

        OtherListItemType.OverlayVisible,
        OtherListItemType.KeepScreenOn,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> Unit

        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
    }
}

/**
 * OtherList の画面を表示する Composable。
 */
@Composable
fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onAppVersionTapped: () -> Unit = {},
    scrollToTopRequest: Int = 0,
) {
    val listState = rememberLazyListState()
    val groupedItems = remember(uiState.items) { uiState.items.groupBy { it.section() } }

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = KoDriverSpacing.small),
    ) {
        otherListSections.forEach { section ->
            val sectionItems = groupedItems[section].orEmpty()
            if (sectionItems.isNotEmpty()) {
                item(key = "section_${section.name}") {
                    OtherListSectionHeader(section)
                    HorizontalDivider()
                }
                items(sectionItems, key = { it.id }) { item ->
                    OtherListItem(
                        item = item,
                        uiState = uiState,
                        onOverlayVisibleChange = onOverlayVisibleChange,
                        onKeepScreenOnChange = onKeepScreenOnChange,
                        onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                        onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                        onStartupEnabledChange = onStartupEnabledChange,
                        onItemClick = onItemClick,
                    )
                    HorizontalDivider()
                }
            }
        }
        if (uiState.appVersionLabel.isNotBlank() && uiState.appVersion.isNotBlank()) {
            item(key = "app_version") {
                OtherAppVersionListItem(
                    appVersionLabel = uiState.appVersionLabel,
                    appVersion = uiState.appVersion,
                    onAppVersionTapped = onAppVersionTapped,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun OtherListSectionHeader(section: OtherListSection) {
    Text(
        text = otherListSectionTitle(section),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = KoDriverSpacing.large,
                    top = KoDriverSpacing.large,
                    end = KoDriverSpacing.large,
                    bottom = KoDriverSpacing.extraSmall,
                ),
    )
}

private fun otherListItemSupportingContent(
    item: OtherListItemType,
    uiState: OtherListUiState,
): (@Composable () -> Unit)? =
    when (item) {
        OtherListItemType.GitHubRepository -> {
            { Text(stringResource(Res.string.item_github_repository_star_request)) }
        }

        OtherListItemType.Volume -> {
            { Text(stringResource(Res.string.item_volume_summary, uiState.soundVolume, uiState.deviceVolume)) }
        }

        OtherListItemType.Voice -> {
            {
                Text(
                    if (uiState.voiceId == VOICE_ID_UNSPECIFIED) {
                        stringResource(Res.string.voice_system_default)
                    } else {
                        uiState.voiceId
                    },
                )
            }
        }

        OtherListItemType.Theme -> {
            {
                val label =
                    when (uiState.themeMode) {
                        ThemeMode.SYSTEM -> Res.string.theme_system
                        ThemeMode.LIGHT -> Res.string.theme_light
                        ThemeMode.DARK -> Res.string.theme_dark
                    }
                Text(stringResource(label))
            }
        }

        OtherListItemType.ReadoutStartSound -> {
            {
                val label =
                    when (uiState.readoutStartSoundType) {
                        ReadoutStartSoundType.FORMULA_RADIO -> Res.string.readout_start_sound_formula_radio
                        ReadoutStartSoundType.ELECTRONIC_NOISE -> Res.string.readout_start_sound_electronic_noise
                    }
                Text(stringResource(label))
            }
        }

        else -> {
            null
        }
    }

@Composable
private fun OtherListItem(
    item: OtherListItemType,
    uiState: OtherListUiState,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    onItemClick: (OtherListItemType) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val openAccessLocalNetworkPermissionSettings = rememberOpenAccessLocalNetworkPermissionSettings()
    val openTtsSettings = rememberOpenTtsSettings()
    val onOverlayVisibleChangeWithHaptic: (Boolean) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onOverlayVisibleChange(it)
    }
    val onKeepScreenOnChangeWithHaptic: (Boolean) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onKeepScreenOnChange(it)
    }
    val onDynamicColorEnabledChangeWithHaptic: (Boolean) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onDynamicColorEnabledChange(it)
    }
    val onHapticFeedbackEnabledChangeWithHaptic: (Boolean) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onHapticFeedbackEnabledChange(it)
    }
    val onStartupEnabledChangeWithHaptic: (Boolean) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onStartupEnabledChange(it)
    }
    val isSelected = item == uiState.selectedItem
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(durationMillis = 500),
        label = "otherListItemContainerColor",
    )
    val headlineColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(durationMillis = 500),
        label = "otherListItemHeadlineColor",
    )
    val iconColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 500),
        label = "otherListItemIconColor",
    )

    CenteredListItem(
        headlineContent = { Text(otherItemDisplayName(item)) },
        supportingContent = otherListItemSupportingContent(item, uiState),
        leadingContent = {
            OtherListItemLeadingIcon(item, uiState.hasAppUpdate, uiState.accessLocalNetworkPermissionGranted)
        },
        trailingContent = {
            when (item) {
                OtherListItemType.OverlayVisible -> {
                    Switch(
                        checked = uiState.overlayVisible,
                        onCheckedChange = onOverlayVisibleChangeWithHaptic,
                    )
                }

                OtherListItemType.KeepScreenOn -> {
                    Switch(
                        checked = uiState.keepScreenOn,
                        onCheckedChange = onKeepScreenOnChangeWithHaptic,
                    )
                }

                OtherListItemType.DynamicColor -> {
                    Switch(
                        checked = uiState.dynamicColorEnabled,
                        onCheckedChange = onDynamicColorEnabledChangeWithHaptic,
                    )
                }

                OtherListItemType.HapticFeedback -> {
                    Switch(
                        checked = uiState.hapticFeedbackEnabled,
                        onCheckedChange = onHapticFeedbackEnabledChangeWithHaptic,
                    )
                }

                OtherListItemType.Startup -> {
                    Switch(
                        checked = uiState.startupEnabled,
                        onCheckedChange = onStartupEnabledChangeWithHaptic,
                    )
                }

                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.Voice,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.WindowsSpeechUnavailable,
                OtherListItemType.Theme,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayBackgroundOpacity,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
                OtherListItemType.DebugState,
                -> {
                    OtherListItemTrailingIcon(item)
                }
            }
        },
        containerColor = containerColor,
        headlineColor = headlineColor,
        iconColor = iconColor,
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics { selected = isSelected }
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    handleOtherListItemClick(
                        item = item,
                        uiState = uiState,
                        onOverlayVisibleChange = onOverlayVisibleChange,
                        onKeepScreenOnChange = onKeepScreenOnChange,
                        onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                        onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                        onStartupEnabledChange = onStartupEnabledChange,
                        openAccessLocalNetworkPermissionSettings = openAccessLocalNetworkPermissionSettings,
                        openTtsSettings = openTtsSettings,
                        onItemClick = onItemClick,
                    )
                },
    )
}

@Suppress("LongParameterList")
private fun handleOtherListItemClick(
    item: OtherListItemType,
    uiState: OtherListUiState,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    openAccessLocalNetworkPermissionSettings: () -> Unit,
    openTtsSettings: () -> Unit,
    onItemClick: (OtherListItemType) -> Unit,
) {
    when (item) {
        OtherListItemType.OverlayVisible -> {
            onOverlayVisibleChange(!uiState.overlayVisible)
        }

        OtherListItemType.KeepScreenOn -> {
            onKeepScreenOnChange(!uiState.keepScreenOn)
        }

        OtherListItemType.DynamicColor -> {
            onDynamicColorEnabledChange(!uiState.dynamicColorEnabled)
        }

        OtherListItemType.HapticFeedback -> {
            onHapticFeedbackEnabledChange(!uiState.hapticFeedbackEnabled)
        }

        OtherListItemType.Startup -> {
            onStartupEnabledChange(!uiState.startupEnabled)
        }

        OtherListItemType.AccessLocalNetworkPermission -> {
            openAccessLocalNetworkPermissionSettings()
        }

        OtherListItemType.TtsLanguageDataMissing -> {
            openTtsSettings()
        }

        OtherListItemType.WindowsSpeechUnavailable -> {
            onItemClick(item)
        }

        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.Theme,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> {
            onItemClick(item)
        }
    }
}

private const val DEBUG_STATE_TAP_THRESHOLD = 5
private val DEBUG_STATE_TAP_TIMEOUT = 1.seconds

@Composable
private fun OtherAppVersionListItem(
    appVersionLabel: String,
    appVersion: String,
    onAppVersionTapped: () -> Unit,
) {
    if (appVersionLabel.isBlank() || appVersion.isBlank()) return

    val haptic = LocalHapticFeedback.current
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapMark by remember { mutableStateOf<TimeMark?>(null) }

    ListItem(
        headlineContent = { Text(appVersionLabel) },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
            )
        },
        trailingContent = {
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    val now = TimeSource.Monotonic.markNow()
                    val elapsedSinceLastTap = lastTapMark?.elapsedNow()
                    tapCount =
                        if (elapsedSinceLastTap != null && elapsedSinceLastTap < DEBUG_STATE_TAP_TIMEOUT) {
                            tapCount + 1
                        } else {
                            1
                        }
                    lastTapMark = now
                    if (tapCount >= DEBUG_STATE_TAP_THRESHOLD) {
                        tapCount = 0
                        onAppVersionTapped()
                    }
                },
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    KoDriverTheme {
        OtherListPane(
            uiState = OtherListUiState(),
            onItemClick = {},
            onOverlayVisibleChange = {},
            onKeepScreenOnChange = {},
            onDynamicColorEnabledChange = {},
            onHapticFeedbackEnabledChange = {},
            onStartupEnabledChange = {},
        )
    }
}

private val centeredListItemMinHeight = 56.dp
private val centeredListItemHorizontalPadding = 16.dp
private val centeredListItemVerticalPadding = 8.dp
private val centeredListItemContentSpacing = 16.dp

/**
 * M3 の ListItem 相当の見た目で、先頭・末尾のアイコンを常に縦中央に揃える一覧項目。
 *
 * M3 の ListItem は副テキストが折り返して3行になると先頭・末尾の要素を上揃えにするため、
 * 狭いペインで音量の副テキストが折り返したときにアイコンが行の中央から外れる。それを避けるため自前で組む。
 */
@Composable
private fun CenteredListItem(
    headlineContent: @Composable () -> Unit,
    leadingContent: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit,
    containerColor: Color,
    headlineColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .background(containerColor)
                .heightIn(min = centeredListItemMinHeight)
                .padding(
                    horizontal = centeredListItemHorizontalPadding,
                    vertical = centeredListItemVerticalPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(centeredListItemContentSpacing),
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box { leadingContent() }
        }
        Column(modifier = Modifier.weight(1f)) {
            CompositionLocalProvider(LocalContentColor provides headlineColor) {
                ProvideTextStyle(MaterialTheme.typography.bodyLarge, content = headlineContent)
            }
            if (supportingContent != null) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium, content = supportingContent)
                }
            }
        }
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            Box { trailingContent() }
        }
    }
}

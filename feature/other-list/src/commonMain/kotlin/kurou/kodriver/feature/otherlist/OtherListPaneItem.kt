package kurou.kodriver.feature.otherlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverSwitch
import kurou.kodriver.core.designsystem.listRowSelectionAnimationSpec
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.console_ip_not_configured
import kurou.kodriver.feature.otherlist.generated.resources.item_github_repository_star_request
import kurou.kodriver.feature.otherlist.generated.resources.item_microphone_cut_quiet_sound_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_microphone_cut_quiet_sound_summary_off
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_background_opacity_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_visible_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_startup_disabled_by_os
import kurou.kodriver.feature.otherlist.generated.resources.item_voice_pitch_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_voice_speed_summary
import kurou.kodriver.feature.otherlist.generated.resources.item_volume_summary
import kurou.kodriver.feature.otherlist.generated.resources.local_network_permission_granted
import kurou.kodriver.feature.otherlist.generated.resources.local_network_permission_not_granted
import kurou.kodriver.feature.otherlist.generated.resources.local_network_permission_open_settings
import kurou.kodriver.feature.otherlist.generated.resources.readout_start_sound_electronic_noise
import kurou.kodriver.feature.otherlist.generated.resources.readout_start_sound_formula_radio
import kurou.kodriver.feature.otherlist.generated.resources.server_ip_not_configured
import kurou.kodriver.feature.otherlist.generated.resources.theme_dark
import kurou.kodriver.feature.otherlist.generated.resources.theme_light
import kurou.kodriver.feature.otherlist.generated.resources.theme_system
import kurou.kodriver.feature.otherlist.generated.resources.voice_system_default
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
internal fun OtherListSectionHeader(section: OtherListSection) {
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

private fun otherSettingValueSupportingContent(
    item: OtherListItemType,
    uiState: OtherListUiState,
): @Composable () -> Unit =
    when (item) {
        OtherListItemType.Volume -> {
            { Text(stringResource(Res.string.item_volume_summary, uiState.soundVolume, uiState.deviceVolume)) }
        }

        OtherListItemType.VoiceSpeed -> {
            {
                val speed = (round(uiState.voiceSpeed * 10) / 10).toString()
                Text(stringResource(Res.string.item_voice_speed_summary, speed))
            }
        }

        OtherListItemType.AndroidAudioUsage -> {
            { Text(stringResource(uiState.androidAudioUsage.labelRes)) }
        }

        OtherListItemType.VoicePitch -> {
            {
                val pitch = (round(uiState.voicePitch * 10) / 10).toString()
                Text(stringResource(Res.string.item_voice_pitch_summary, pitch))
            }
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

        OtherListItemType.OverlayTextSize -> {
            {
                val label = uiState.overlayTextSize.labelResource()
                Text(stringResource(label))
            }
        }

        OtherListItemType.OverlayTextColor -> {
            { OtherListOverlayTextColorSummary(uiState.overlayTextColor) }
        }

        OtherListItemType.OverlayBackgroundOpacity -> {
            {
                val opacity = uiState.overlayBackgroundOpacity
                Text(stringResource(Res.string.item_overlay_background_opacity_summary, opacity))
            }
        }

        OtherListItemType.MicrophoneCutQuietSound -> {
            { OtherMicrophoneCutQuietSoundSummary(uiState.microphoneCutQuietSoundLevel) }
        }

        else -> {
            error("unexpected item type: $item")
        }
    }

@Composable
private fun OtherServerIpSummary(serverIp: String?) {
    Text(serverIp ?: stringResource(Res.string.server_ip_not_configured))
}

@Composable
private fun OtherThemeSummary(themeMode: ThemeMode) {
    val label =
        when (themeMode) {
            ThemeMode.SYSTEM -> Res.string.theme_system
            ThemeMode.LIGHT -> Res.string.theme_light
            ThemeMode.DARK -> Res.string.theme_dark
        }
    Text(stringResource(label))
}

private fun otherAppSettingsSupportingContent(
    item: OtherListItemType,
    uiState: OtherListUiState,
): (@Composable () -> Unit)? =
    if (item == OtherListItemType.Theme) {
        { OtherThemeSummary(uiState.themeMode) }
    } else if (uiState.startupDisabledByOs) {
        { OtherStartupDisabledByOsSummary() }
    } else {
        null
    }

@Composable
private fun OtherMicrophoneCutQuietSoundSummary(level: Int) {
    Text(
        if (level == MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN) {
            stringResource(Res.string.item_microphone_cut_quiet_sound_summary_off)
        } else {
            stringResource(Res.string.item_microphone_cut_quiet_sound_summary, level)
        },
    )
}

@Composable
private fun OtherStartupDisabledByOsSummary() {
    Text(
        text = stringResource(Res.string.item_startup_disabled_by_os),
        color = MaterialTheme.colorScheme.error,
    )
}

private fun otherListItemSupportingContent(
    item: OtherListItemType,
    uiState: OtherListUiState,
): (@Composable () -> Unit)? =
    when (item) {
        OtherListItemType.ServerIp -> {
            { OtherServerIpSummary(uiState.serverIp) }
        }

        OtherListItemType.ConsoleIp -> {
            {
                Text(
                    if (uiState.consoleAddress.isNullOrBlank()) {
                        stringResource(Res.string.console_ip_not_configured)
                    } else {
                        "${uiState.consoleAddress}:${uiState.consolePort}"
                    },
                )
            }
        }

        OtherListItemType.AccessLocalNetworkPermission -> {
            {
                if (uiState.accessLocalNetworkPermissionGranted) {
                    Text(stringResource(Res.string.local_network_permission_granted))
                } else {
                    Column {
                        Text(stringResource(Res.string.local_network_permission_not_granted))
                        Text(stringResource(Res.string.local_network_permission_open_settings))
                    }
                }
            }
        }

        OtherListItemType.GitHubRepository -> {
            { Text(stringResource(Res.string.item_github_repository_star_request)) }
        }

        OtherListItemType.OverlayVisible -> {
            { Text(stringResource(Res.string.item_overlay_visible_summary)) }
        }

        OtherListItemType.Volume,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        OtherListItemType.AndroidAudioUsage,
        OtherListItemType.Voice,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.MicrophoneCutQuietSound,
        -> {
            otherSettingValueSupportingContent(item, uiState)
        }

        OtherListItemType.Theme,
        OtherListItemType.Startup,
        -> {
            otherAppSettingsSupportingContent(item, uiState)
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
internal fun OtherListItem(
    item: OtherListItemType,
    uiState: OtherListUiState,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    onItemClick: (OtherListItemType) -> Unit,
    rowFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val openAccessLocalNetworkPermissionSettings = rememberOpenAccessLocalNetworkPermissionSettings()
    val openTtsSettings = rememberOpenTtsSettings()
    val isSelected = item == uiState.selectedItem
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = listRowSelectionAnimationSpec(),
        label = "otherListItemContainerColor",
    )
    val isDestructive = item.isDestructive()
    val headlineColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                isDestructive -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
        animationSpec = listRowSelectionAnimationSpec(),
        label = "otherListItemHeadlineColor",
    )
    val iconColor by animateColorAsState(
        targetValue =
            when {
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                isDestructive -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = listRowSelectionAnimationSpec(),
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
                    KoDriverSwitch(
                        checked = uiState.overlayVisible,
                        onCheckedChange = onOverlayVisibleChange,
                    )
                }

                OtherListItemType.KeepScreenOn -> {
                    KoDriverSwitch(
                        checked = uiState.keepScreenOn,
                        onCheckedChange = onKeepScreenOnChange,
                    )
                }

                OtherListItemType.DynamicColor -> {
                    KoDriverSwitch(
                        checked = uiState.dynamicColorEnabled,
                        onCheckedChange = onDynamicColorEnabledChange,
                    )
                }

                OtherListItemType.HapticFeedback -> {
                    KoDriverSwitch(
                        checked = uiState.hapticFeedbackEnabled,
                        onCheckedChange = onHapticFeedbackEnabledChange,
                    )
                }

                OtherListItemType.Startup -> {
                    KoDriverSwitch(
                        checked = uiState.startupEnabled,
                        onCheckedChange = onStartupEnabledChange,
                    )
                }

                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.Voice,
                OtherListItemType.VoiceSpeed,
                OtherListItemType.VoicePitch,
                OtherListItemType.AndroidAudioUsage,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.WindowsSpeechUnavailable,
                OtherListItemType.Theme,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.OverlayTextColor,
                OtherListItemType.OverlayBackgroundOpacity,
                OtherListItemType.MicrophoneCutQuietSound,
                OtherListItemType.ShowLogs,
                OtherListItemType.DeleteAllLogs,
                OtherListItemType.ResetAutoReadoutSettings,
                OtherListItemType.ResetManualReadoutSettings,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
                OtherListItemType.DebugState,
                -> {
                    OtherListItemTrailingIcon(item, isSelected)
                }
            }
        },
        containerColor = containerColor,
        headlineColor = headlineColor,
        iconColor = iconColor,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { selected = isSelected }
                .focusRequester(rowFocusRequester)
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
        OtherListItemType.DeleteAllLogs,
        OtherListItemType.ResetAutoReadoutSettings,
        OtherListItemType.ResetManualReadoutSettings,
        -> {
            onItemClick(item)
        }

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

        OtherListItemType.AndroidAudioUsage -> {
            onItemClick(item)
        }

        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.Theme,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.MicrophoneCutQuietSound,
        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.ShowLogs,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> {
            onItemClick(item)
        }
    }
}

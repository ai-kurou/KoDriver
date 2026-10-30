package kurou.kodriver.feature.otherlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_alarm
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_assistant
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_communication
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_media
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_notification
import kurou.kodriver.feature.otherlist.generated.resources.audio_usage_ringtone
import kurou.kodriver.feature.otherlist.generated.resources.auto_readout_settings_reset_item
import kurou.kodriver.feature.otherlist.generated.resources.item_access_local_network_permission
import kurou.kodriver.feature.otherlist.generated.resources.item_android_audio_usage
import kurou.kodriver.feature.otherlist.generated.resources.item_console_ip
import kurou.kodriver.feature.otherlist.generated.resources.item_debug_state
import kurou.kodriver.feature.otherlist.generated.resources.item_delete_all_logs
import kurou.kodriver.feature.otherlist.generated.resources.item_dynamic_color
import kurou.kodriver.feature.otherlist.generated.resources.item_feedback
import kurou.kodriver.feature.otherlist.generated.resources.item_github_repository
import kurou.kodriver.feature.otherlist.generated.resources.item_haptic_feedback
import kurou.kodriver.feature.otherlist.generated.resources.item_keep_screen_on
import kurou.kodriver.feature.otherlist.generated.resources.item_license
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_background_opacity
import kurou.kodriver.feature.otherlist.generated.resources.item_overlay_text_color
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
import kurou.kodriver.feature.otherlist.generated.resources.item_voice_pitch
import kurou.kodriver.feature.otherlist.generated.resources.item_voice_speed
import kurou.kodriver.feature.otherlist.generated.resources.item_volume
import kurou.kodriver.feature.otherlist.generated.resources.item_windows_speech_unavailable
import kurou.kodriver.feature.otherlist.generated.resources.manual_readout_settings_reset_item
import kurou.kodriver.feature.otherlist.generated.resources.section_app_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_connection_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_data_log
import kurou.kodriver.feature.otherlist.generated.resources.section_information
import kurou.kodriver.feature.otherlist.generated.resources.section_overlay_settings
import kurou.kodriver.feature.otherlist.generated.resources.section_readout_settings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class OtherListSection {
    ConnectionSettings,
    ReadoutSettings,
    OverlaySettings,
    AppSettings,
    DataLog,
    Information,
}

internal val otherListSections =
    listOf(
        OtherListSection.ConnectionSettings,
        OtherListSection.ReadoutSettings,
        OtherListSection.OverlaySettings,
        OtherListSection.AppSettings,
        OtherListSection.DataLog,
        OtherListSection.Information,
    )

internal fun OtherListItemType.section(): OtherListSection =
    when (this) {
        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        -> OtherListSection.ConnectionSettings

        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        OtherListItemType.AndroidAudioUsage,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> OtherListSection.ReadoutSettings

        OtherListItemType.OverlayVisible,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayBackgroundOpacity,
        -> OtherListSection.OverlaySettings

        OtherListItemType.KeepScreenOn,
        OtherListItemType.Theme,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> OtherListSection.AppSettings

        OtherListItemType.DeleteAllLogs,
        OtherListItemType.ResetAutoReadoutSettings,
        OtherListItemType.ResetManualReadoutSettings,
        -> OtherListSection.DataLog

        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        -> OtherListSection.Information

        OtherListItemType.DebugState -> OtherListSection.Information
    }

@Composable
private fun otherReadoutSettingsResetItemDisplayName(itemType: OtherListItemType): String =
    if (itemType == OtherListItemType.ResetManualReadoutSettings) {
        stringResource(Res.string.manual_readout_settings_reset_item)
    } else {
        stringResource(Res.string.auto_readout_settings_reset_item)
    }

@Composable
internal fun otherItemDisplayName(itemType: OtherListItemType): String =
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

        is OtherListItemType.ReadoutSettings -> {
            otherReadoutSettingsItemDisplayName(itemType)
        }

        is OtherListItemType.OverlaySettings -> {
            otherOverlaySettingsItemDisplayName(itemType)
        }

        OtherListItemType.ResetAutoReadoutSettings,
        OtherListItemType.ResetManualReadoutSettings,
        -> {
            otherReadoutSettingsResetItemDisplayName(itemType)
        }

        OtherListItemType.DeleteAllLogs -> {
            stringResource(Res.string.item_delete_all_logs)
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

        is OtherListItemType.AppSettings -> {
            otherAppSettingsItemDisplayName(itemType)
        }
    }

@Composable
private fun otherReadoutSettingsItemDisplayName(itemType: OtherListItemType.ReadoutSettings): String =
    when (itemType) {
        OtherListItemType.Volume -> stringResource(Res.string.item_volume)
        OtherListItemType.Voice -> stringResource(Res.string.item_voice)
        OtherListItemType.VoiceSpeed -> stringResource(Res.string.item_voice_speed)
        OtherListItemType.VoicePitch -> stringResource(Res.string.item_voice_pitch)
        OtherListItemType.AndroidAudioUsage -> stringResource(Res.string.item_android_audio_usage)
        OtherListItemType.ReadoutStartSound -> stringResource(Res.string.item_readout_start_sound)
        OtherListItemType.TtsEngineMissing -> stringResource(Res.string.item_tts_engine_missing)
        OtherListItemType.TtsLanguageDataMissing -> stringResource(Res.string.item_tts_language_data_missing)
        OtherListItemType.WindowsSpeechUnavailable -> stringResource(Res.string.item_windows_speech_unavailable)
    }

@Composable
private fun otherAppSettingsItemDisplayName(itemType: OtherListItemType.AppSettings): String =
    when (itemType) {
        OtherListItemType.KeepScreenOn -> stringResource(Res.string.item_keep_screen_on)
        OtherListItemType.Theme -> stringResource(Res.string.item_theme)
        OtherListItemType.DynamicColor -> stringResource(Res.string.item_dynamic_color)
        OtherListItemType.HapticFeedback -> stringResource(Res.string.item_haptic_feedback)
        OtherListItemType.Startup -> stringResource(Res.string.item_startup)
    }

@Composable
private fun otherOverlaySettingsItemDisplayName(itemType: OtherListItemType.OverlaySettings): String =
    when (itemType) {
        OtherListItemType.OverlayVisible -> stringResource(Res.string.item_overlay_visible)
        OtherListItemType.OverlayTextSize -> stringResource(Res.string.item_overlay_text_size)
        OtherListItemType.OverlayTextColor -> stringResource(Res.string.item_overlay_text_color)
        OtherListItemType.OverlayBackgroundOpacity -> stringResource(Res.string.item_overlay_background_opacity)
    }

@Composable
internal fun otherListSectionTitle(section: OtherListSection): String =
    when (section) {
        OtherListSection.ConnectionSettings -> stringResource(Res.string.section_connection_settings)
        OtherListSection.ReadoutSettings -> stringResource(Res.string.section_readout_settings)
        OtherListSection.OverlaySettings -> stringResource(Res.string.section_overlay_settings)
        OtherListSection.AppSettings -> stringResource(Res.string.section_app_settings)
        OtherListSection.DataLog -> stringResource(Res.string.section_data_log)
        OtherListSection.Information -> stringResource(Res.string.section_information)
    }

@Composable
internal fun OtherListItemLeadingIcon(
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
        BadgedBox(
            badge = {
                AnimatedVisibility(
                    visible = showBadge,
                    enter =
                        scaleIn(animationSpec = tween(durationMillis = 300)) +
                            fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit =
                        scaleOut(animationSpec = tween(durationMillis = 200)) +
                            fadeOut(animationSpec = tween(durationMillis = 200)),
                ) {
                    Badge()
                }
            },
        ) {
            Icon(imageVector = imageVector, contentDescription = null)
        }
    } else {
        Icon(imageVector = imageVector, contentDescription = null)
    }
}

@Composable
internal fun OtherListItemTrailingIcon(
    itemType: OtherListItemType,
    isSelected: Boolean,
) {
    when (itemType) {
        OtherListItemType.ServerIp -> {
            OtherListGameTagsWithChevron(
                simulators = listOf(Simulator.LmuWindows, Simulator.AceWindows),
                isSelected = isSelected,
            )
        }

        OtherListItemType.ConsoleIp -> {
            OtherListGameTagsWithChevron(
                simulators = listOf(Simulator.Gt7Ps5),
                isSelected = isSelected,
            )
        }

        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        OtherListItemType.AndroidAudioUsage,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> {
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)
        }

        OtherListItemType.ReadoutStartSound,
        OtherListItemType.Theme,
        OtherListItemType.OverlayTextSize,
        -> {
            Icon(imageVector = Icons.Outlined.UnfoldMore, contentDescription = null)
        }

        OtherListItemType.DeleteAllLogs,
        OtherListItemType.ResetAutoReadoutSettings,
        OtherListItemType.ResetManualReadoutSettings,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayVisible,
        OtherListItemType.KeepScreenOn,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        -> {}

        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> {
            Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
        }
    }
}

internal val AndroidAudioUsage.labelRes: StringResource
    get() =
        when (this) {
            AndroidAudioUsage.MEDIA -> Res.string.audio_usage_media
            AndroidAudioUsage.COMMUNICATION -> Res.string.audio_usage_communication
            AndroidAudioUsage.RINGTONE -> Res.string.audio_usage_ringtone
            AndroidAudioUsage.NOTIFICATION -> Res.string.audio_usage_notification
            AndroidAudioUsage.ALARM -> Res.string.audio_usage_alarm
            AndroidAudioUsage.ASSISTANT -> Res.string.audio_usage_assistant
        }

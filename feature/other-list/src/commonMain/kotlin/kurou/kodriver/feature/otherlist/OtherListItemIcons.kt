package kurou.kodriver.feature.otherlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

internal fun otherListItemLeadingIconVector(itemType: OtherListItemType): ImageVector =
    when (itemType) {
        OtherListItemType.AccessLocalNetworkPermission -> Icons.Outlined.Wifi

        OtherListItemType.ServerIp -> Icons.Outlined.Computer

        OtherListItemType.ConsoleIp -> Icons.Outlined.SportsEsports

        OtherListItemType.Volume,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.ReadoutStartSound,
        -> otherReadoutSettingsItemLeadingIconVector(itemType)

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

private fun otherReadoutSettingsItemLeadingIconVector(itemType: OtherListItemType): ImageVector =
    when (itemType) {
        OtherListItemType.Volume -> Icons.AutoMirrored.Outlined.VolumeUp
        OtherListItemType.VoiceSpeed -> Icons.Outlined.Speed
        OtherListItemType.ReadoutStartSound -> Icons.Outlined.MusicNote
        else -> error("unexpected item type: $itemType")
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

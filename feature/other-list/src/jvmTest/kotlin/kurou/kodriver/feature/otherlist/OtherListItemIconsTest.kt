package kurou.kodriver.feature.otherlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.GraphicEq
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
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherListItemIconsTest {
    @Test
    fun `全25項目のアイコンはグループ化前と同じ`() {
        val expectedIcons =
            mapOf(
                OtherListItemType.AccessLocalNetworkPermission to Icons.Outlined.Wifi,
                OtherListItemType.ServerIp to Icons.Outlined.Computer,
                OtherListItemType.ConsoleIp to Icons.Outlined.SportsEsports,
                OtherListItemType.GitHubRepository to Icons.Outlined.Star,
                OtherListItemType.ReleasePage to Icons.Outlined.NewReleases,
                OtherListItemType.Feedback to Icons.Outlined.Feedback,
                OtherListItemType.License to Icons.Outlined.Description,
                OtherListItemType.DebugState to Icons.Outlined.Code,
                OtherListItemType.Volume to Icons.AutoMirrored.Outlined.VolumeUp,
                OtherListItemType.VoiceSpeed to Icons.Outlined.Speed,
                OtherListItemType.VoicePitch to Icons.Outlined.GraphicEq,
                OtherListItemType.AndroidAudioUsage to Icons.Outlined.GraphicEq,
                OtherListItemType.ReadoutStartSound to Icons.Outlined.MusicNote,
                OtherListItemType.OverlayVisible to Icons.Outlined.Layers,
                OtherListItemType.OverlayTextSize to Icons.Outlined.FormatSize,
                OtherListItemType.OverlayTextColor to Icons.Outlined.FormatColorText,
                OtherListItemType.OverlayBackgroundOpacity to Icons.Outlined.Opacity,
                OtherListItemType.KeepScreenOn to Icons.Outlined.BedtimeOff,
                OtherListItemType.Theme to Icons.Outlined.BrightnessHigh,
                OtherListItemType.DynamicColor to Icons.Outlined.Palette,
                OtherListItemType.HapticFeedback to Icons.Outlined.Vibration,
                OtherListItemType.Startup to Icons.Outlined.PowerSettingsNew,
                OtherListItemType.Voice to Icons.Outlined.RecordVoiceOver,
                OtherListItemType.TtsEngineMissing to Icons.Outlined.RecordVoiceOver,
                OtherListItemType.TtsLanguageDataMissing to Icons.Outlined.RecordVoiceOver,
                OtherListItemType.WindowsSpeechUnavailable to Icons.Outlined.RecordVoiceOver,
            )

        assertEquals(OtherListItemType.entries.toSet(), expectedIcons.keys)
        expectedIcons.forEach { (itemType, expectedIcon) ->
            assertEquals(expectedIcon, otherListItemLeadingIconVector(itemType), itemType.id)
        }
    }
}

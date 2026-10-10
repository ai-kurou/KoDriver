package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.othervoicedetail.OtherVoiceDetailPane
import kurou.kodriver.feature.othervoicepitchdetail.OtherVoicePitchDetailPane
import kurou.kodriver.feature.othervoicespeeddetail.OtherVoiceSpeedDetailPane
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPane

/**
 * 読み上げ設定カテゴリ（音量・音声・速度・高さ）の詳細ペインを表示する。
 * `DefaultOtherContent` の循環的複雑度を抑えるため、分岐を切り出している。
 */
@Composable
internal fun OtherReadoutSettingsDetailPane(
    itemType: OtherListItemType,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
) {
    when (itemType) {
        OtherListItemType.Volume -> OtherVolumeDetailPane(canNavigateBack, onBack)
        OtherListItemType.Voice -> OtherVoiceDetailPane(canNavigateBack, onBack)
        OtherListItemType.VoiceSpeed -> OtherVoiceSpeedDetailPane(canNavigateBack, onBack)
        OtherListItemType.VoicePitch -> OtherVoicePitchDetailPane(canNavigateBack, onBack)
        else -> Unit
    }
}

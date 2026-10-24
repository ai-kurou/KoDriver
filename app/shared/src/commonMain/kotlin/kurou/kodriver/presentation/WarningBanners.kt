package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance

// 接続バナーとTTS警告バナーの間に背景色の隙間として見せる1dp。
private val ConnectionTtsBannerGap = 1.dp

/** TTS警告と音量警告を表示する。TTS警告が優先され、表示中は音量警告を隠す。 */
@Composable
internal fun WarningBanners(
    isConnectionBannerVisible: Boolean,
    ttsUnavailableGuidance: TtsUnavailableGuidance?,
    onTtsAction: () -> Unit,
    lowVolumeWarning: LowVolumeWarningUiState,
    onLowVolumeBannerClick: () -> Unit,
    onLowVolumeBannerDismiss: () -> Unit,
) {
    val showLowVolumeBanner = lowVolumeWarning.isVisible && ttsUnavailableGuidance == null
    if (isConnectionBannerVisible && (ttsUnavailableGuidance != null || showLowVolumeBanner)) {
        Spacer(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ConnectionTtsBannerGap)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("connectionTtsBannerGap"),
        )
    }
    TtsUnavailableBannerHost(reason = ttsUnavailableGuidance, onAction = onTtsAction)
    if (showLowVolumeBanner) {
        LowVolumeBanner(
            uiState = lowVolumeWarning,
            onClick = onLowVolumeBannerClick,
            onDismiss = onLowVolumeBannerDismiss,
        )
    }
}

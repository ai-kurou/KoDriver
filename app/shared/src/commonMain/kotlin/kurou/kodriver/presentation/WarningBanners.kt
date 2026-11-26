package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance

// 接続バナーとTTS警告バナーの間に背景色の隙間として見せる1dp。
private val ConnectionTtsBannerGap = 1.dp

internal const val BANNER_ENTER_MILLIS = 300
internal const val BANNER_EXIT_MILLIS = 200

// バナーの下の画面内容が跳ねないよう、高さも伸縮させる。下端を基準にすることで、上から降りてくる見た目にする。
internal val BannerEnterTransition: EnterTransition =
    expandVertically(tween(durationMillis = BANNER_ENTER_MILLIS), expandFrom = Alignment.Bottom) +
        fadeIn(animationSpec = tween(durationMillis = BANNER_ENTER_MILLIS))

internal val BannerExitTransition: ExitTransition =
    shrinkVertically(tween(durationMillis = BANNER_EXIT_MILLIS), shrinkTowards = Alignment.Bottom) +
        fadeOut(animationSpec = tween(durationMillis = BANNER_EXIT_MILLIS))

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
    // 非表示への切り替えで入力が消えても、退場アニメーション中は最後の表示内容を保つ。
    var lastTtsGuidance by remember { mutableStateOf(ttsUnavailableGuidance) }
    var lastLowVolumeWarning by remember { mutableStateOf(lowVolumeWarning) }
    if (ttsUnavailableGuidance != null) {
        lastTtsGuidance = ttsUnavailableGuidance
    }
    if (showLowVolumeBanner) {
        lastLowVolumeWarning = lowVolumeWarning
    }
    AnimatedVisibility(
        visible = ttsUnavailableGuidance != null,
        enter = BannerEnterTransition,
        exit = BannerExitTransition,
    ) {
        Column {
            if (isConnectionBannerVisible) {
                ConnectionWarningBannerGap()
            }
            TtsUnavailableBannerHost(reason = lastTtsGuidance, onAction = onTtsAction)
        }
    }
    AnimatedVisibility(
        visible = showLowVolumeBanner,
        enter = BannerEnterTransition,
        exit = BannerExitTransition,
    ) {
        Column {
            if (isConnectionBannerVisible) {
                ConnectionWarningBannerGap()
            }
            LowVolumeBanner(
                uiState = lastLowVolumeWarning,
                onClick = onLowVolumeBannerClick,
                onDismiss = onLowVolumeBannerDismiss,
            )
        }
    }
}

@Composable
private fun ConnectionWarningBannerGap() {
    Spacer(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ConnectionTtsBannerGap)
                .background(MaterialTheme.colorScheme.background)
                .testTag("connectionTtsBannerGap"),
    )
}

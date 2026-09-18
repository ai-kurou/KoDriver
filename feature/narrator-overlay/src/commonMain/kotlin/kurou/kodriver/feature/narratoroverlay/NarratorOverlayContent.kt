package kurou.kodriver.feature.narratoroverlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.OverlayTextSize
import org.koin.compose.viewmodel.koinViewModel

/**
 * オーバーレイHUDの背景色（薄い灰色）。ゲーム画面に重ねて表示する専用ウィンドウのための固定色で、
 * アプリ本体の[KoDriverTheme]のカラースキームとは独立して定義する。実際に描画する際は、
 * ユーザーが設定した[NarratorOverlayUiState.backgroundOpacity]に応じたアルファ値をこの色へ適用する。
 */
private val NarratorOverlayBackgroundColor = Color(0xFFEDEDED)

/**
 * オーバーレイHUDの文字色（黄色系）。ゲーム画面上での視認性を優先した固定色とする。
 */
private val NarratorOverlayTextColor = Color(0xFFFFC107)

/**
 * NarratorOverlayContent を提供する公開関数。
 */
@Composable
fun NarratorOverlayContent(modifier: Modifier = Modifier) {
    val viewModel: NarratorOverlayViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NarratorOverlayContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun NarratorOverlayContent(
    uiState: NarratorOverlayUiState = NarratorOverlayUiState(),
    modifier: Modifier = Modifier,
) {
    // このオーバーレイはアプリ本体の Window とは別の独立した Window に単体でホストされるため、
    // 上位から KoDriverTheme が供給されない。MaterialTheme.typography を使うためにここで自前に適用する。
    KoDriverTheme {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(NarratorOverlayBackgroundColor.copy(alpha = uiState.backgroundOpacity / 100f))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.latestTelemetryLog?.narratedText.orEmpty(),
                color = NarratorOverlayTextColor,
                style = narratorOverlayTextStyle(uiState.overlayTextSize),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun narratorOverlayTextStyle(overlayTextSize: OverlayTextSize): TextStyle =
    when (overlayTextSize) {
        OverlayTextSize.SMALL -> MaterialTheme.typography.titleMedium
        OverlayTextSize.MEDIUM -> MaterialTheme.typography.headlineSmall
        OverlayTextSize.LARGE -> MaterialTheme.typography.headlineLarge
    }

@Preview(showBackground = true)
@Composable
private fun NarratorOverlayContentPreview() {
    NarratorOverlayContent(
        uiState = NarratorOverlayUiState(latestTelemetryLog = null),
    )
}

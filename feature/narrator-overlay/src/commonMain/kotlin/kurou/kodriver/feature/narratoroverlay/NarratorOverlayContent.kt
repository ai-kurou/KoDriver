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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * オーバーレイHUDの背景色（薄い灰色）。ゲーム画面に重ねて表示する専用ウィンドウのための固定色で、
 * アプリ本体の[KoDriverTheme]のカラースキームとは独立して定義する。
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
                    .background(NarratorOverlayBackgroundColor)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text = uiState.latestTelemetryLog?.narratedText.orEmpty(),
                color = NarratorOverlayTextColor,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NarratorOverlayContentPreview() {
    NarratorOverlayContent(
        uiState = NarratorOverlayUiState(latestTelemetryLog = null),
    )
}

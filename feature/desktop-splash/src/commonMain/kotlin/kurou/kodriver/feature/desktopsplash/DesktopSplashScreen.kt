package kurou.kodriver.feature.desktopsplash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.desktopsplash.generated.resources.Res
import kurou.kodriver.feature.desktopsplash.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

private const val APP_ICON_SIZE_DP = 96
private const val PROGRESS_INDICATOR_WIDTH_DP = 200

/**
 * デスクトップアプリ起動中に表示するスプラッシュ画面。
 *
 * 初期化がどのフェーズまで進んでいるかを [DesktopSplashUiState] から受け取り、
 * アプリアイコン・進捗バー・現在のフェーズ名を中央に表示する。
 */
@Composable
fun DesktopSplashScreen(
    uiState: DesktopSplashUiState,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = "KoDriver",
                modifier = Modifier.size(APP_ICON_SIZE_DP.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.width(PROGRESS_INDICATOR_WIDTH_DP.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.step.displayName,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun DesktopSplashScreenPreview(
    @PreviewParameter(DesktopSplashScreenPreviewParameterProvider::class)
    uiState: DesktopSplashUiState,
) {
    KoDriverTheme {
        DesktopSplashScreen(uiState = uiState)
    }
}

private class DesktopSplashScreenPreviewParameterProvider : PreviewParameterProvider<DesktopSplashUiState> {
    override val values: Sequence<DesktopSplashUiState> =
        sequenceOf(
            DesktopSplashUiState(step = DesktopSplashStep.INITIALIZING_MODULES),
            DesktopSplashUiState(step = DesktopSplashStep.STARTING_SERVER),
            DesktopSplashUiState(step = DesktopSplashStep.READY),
        )
}

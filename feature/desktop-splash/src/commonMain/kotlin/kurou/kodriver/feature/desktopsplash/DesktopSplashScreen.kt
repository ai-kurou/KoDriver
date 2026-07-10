package kurou.kodriver.feature.desktopsplash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val PROGRESS_INDICATOR_WIDTH_DP = 200

/**
 * デスクトップアプリ起動中に表示するスプラッシュ画面。
 *
 * 初期化がどのフェーズまで進んでいるかを [DesktopSplashUiState] から受け取り、
 * アプリ名・進捗バー・現在のフェーズ名を中央に表示する。
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
            Text(
                text = "KoDriver",
                style = MaterialTheme.typography.headlineMedium,
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

package kurou.kodriver.feature.splash.desktop

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.splash.SplashAppIcon
import kurou.kodriver.feature.splash.SplashBackdrop
import kurou.kodriver.feature.splash.SplashVersionLabel
import org.jetbrains.compose.resources.stringResource

private const val PROGRESS_INDICATOR_WIDTH_DP = 200

/** 起動完了時に光が画面を横切るのにかける時間（ミリ秒）。この間はスプラッシュ画面を保つ。 */
internal const val SHINE_DURATION_MILLIS = 900

/**
 * デスクトップアプリ起動中に表示するスプラッシュ画面。
 *
 * 初期化がどのフェーズまで進んでいるかを [DesktopSplashUiState] から受け取り、
 * アプリアイコン・進捗バー・現在のフェーズ名を左側に、自己ベストラップの背景画像を右側に表示する。
 * フェーズ名の下にバージョン（[appVersion]）を表示する。
 * 起動が完了すると光が画面を横切り、終わったところで [onReadyAnimationFinished] を一度呼ぶ。
 * 呼び出し側はこの通知を受けてからメイン画面へ切り替えると、光を最後まで見せられる。
 */
@Composable
public fun DesktopSplashScreen(
    uiState: DesktopSplashUiState,
    appVersion: String,
    modifier: Modifier = Modifier,
    onReadyAnimationFinished: () -> Unit = {},
) {
    val shineProgress = remember { Animatable(0f) }
    val currentOnFinished by rememberUpdatedState(onReadyAnimationFinished)
    LaunchedEffect(uiState.isReady) {
        if (uiState.isReady) {
            shineProgress.animateTo(1f, tween(SHINE_DURATION_MILLIS, easing = LinearOutSlowInEasing))
            currentOnFinished()
        }
    }
    DesktopSplashContent(
        uiState = uiState,
        appVersion = appVersion,
        shineProgress = shineProgress.value,
        modifier = modifier,
    )
}

/** [DesktopSplashScreen] の状態を持たない本体。[shineProgress]（0〜1）は光の位置で、スクリーンショットテストから固定して撮れる。 */
@Composable
internal fun DesktopSplashContent(
    uiState: DesktopSplashUiState,
    appVersion: String,
    shineProgress: Float,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        SplashBackdrop(shineProgress = shineProgress) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = KoDriverSpacing.extraLarge * 2),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                SplashAppIcon()
                Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
                val animatedProgress by animateFloatAsState(
                    targetValue = uiState.progress,
                    label = "desktopSplashProgress",
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.width(PROGRESS_INDICATOR_WIDTH_DP.dp),
                )
                Spacer(modifier = Modifier.height(KoDriverSpacing.medium))
                Crossfade(targetState = uiState.step, label = "desktopSplashStep") { step ->
                    Text(
                        text = stringResource(step.displayNameRes),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(KoDriverSpacing.small))
                SplashVersionLabel(appVersion = appVersion)
            }
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
        DesktopSplashScreen(uiState = uiState, appVersion = "5.3.0")
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

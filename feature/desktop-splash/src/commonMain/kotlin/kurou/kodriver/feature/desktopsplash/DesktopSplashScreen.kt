package kurou.kodriver.feature.desktopsplash

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ReadoutBackground
import kurou.kodriver.core.designsystem.rightAlignedBackgroundImage
import kurou.kodriver.feature.desktopsplash.generated.resources.Res
import kurou.kodriver.feature.desktopsplash.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val APP_ICON_SIZE_DP = 96
private const val PROGRESS_INDICATOR_WIDTH_DP = 200

/** 起動完了時に光が画面を横切るのにかける時間（ミリ秒）。この間はスプラッシュ画面を保つ。 */
internal const val SHINE_DURATION_MILLIS = 900

/** 光の帯の幅の、画面幅に対する割合。 */
internal const val SHINE_BAND_WIDTH_FRACTION = 0.45f

/** 光の帯が最も濃いところの不透明度。 */
internal const val SHINE_PEAK_ALPHA = 0.35f

/**
 * 光の帯の中心の x 座標（px）を返す。[progress] が 0 のとき帯は画面の左外、1 のとき右外にあり、
 * 画面全体を左から右へ横切る。drawWithContent 内はユニットテストできないため切り出している。
 */
internal fun shineBandCenter(
    progress: Float,
    width: Float,
    bandWidth: Float,
): Float = -bandWidth + (width + 2f * bandWidth) * progress

/**
 * 起動完了時に光が画面を横切る演出の描画。[progress] が 0 以下または 1 以上のときは何も描かない。
 * 帯は斜めのグラデーションで、色は [color] を中央で [SHINE_PEAK_ALPHA] まで濃くする。
 */
private fun Modifier.shine(
    progress: Float,
    color: Color,
): Modifier =
    drawWithContent {
        drawContent()
        if (progress > 0f && progress < 1f) {
            val bandWidth = size.width * SHINE_BAND_WIDTH_FRACTION
            val center = shineBandCenter(progress, size.width, bandWidth)
            drawRect(
                brush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                color.copy(alpha = SHINE_PEAK_ALPHA),
                                Color.Transparent,
                            ),
                        start = Offset(center - bandWidth / 2f, 0f),
                        end = Offset(center + bandWidth / 2f, size.height * SHINE_BAND_SLANT_FRACTION),
                    ),
            )
        }
    }

/** 光の帯の傾きを決める、グラデーション終点の y 座標の画面高さに対する割合。 */
private const val SHINE_BAND_SLANT_FRACTION = 0.5f

/**
 * デスクトップアプリ起動中に表示するスプラッシュ画面。
 *
 * 初期化がどのフェーズまで進んでいるかを [DesktopSplashUiState] から受け取り、
 * アプリアイコン・進捗バー・現在のフェーズ名を左側に、自己ベストラップの背景画像を右側に表示する。
 * 起動が完了すると光が画面を横切り、終わったところで [onReadyAnimationFinished] を一度呼ぶ。
 * 呼び出し側はこの通知を受けてからメイン画面へ切り替えると、光を最後まで見せられる。
 */
@Composable
public fun DesktopSplashScreen(
    uiState: DesktopSplashUiState,
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
    DesktopSplashContent(uiState = uiState, shineProgress = shineProgress.value, modifier = modifier)
}

/** [DesktopSplashScreen] の状態を持たない本体。[shineProgress]（0〜1）は光の位置で、スクリーンショットテストから固定して撮れる。 */
@Composable
internal fun DesktopSplashContent(
    uiState: DesktopSplashUiState,
    shineProgress: Float,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val photo = painterResource(ReadoutBackground.BEST_LAP.drawable)
    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .rightAlignedBackgroundImage(photo, surface)
                    .shine(shineProgress, MaterialTheme.colorScheme.primary),
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = KoDriverSpacing.extraLarge * 2),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = "KoDriver",
                    modifier = Modifier.size(APP_ICON_SIZE_DP.dp),
                )
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

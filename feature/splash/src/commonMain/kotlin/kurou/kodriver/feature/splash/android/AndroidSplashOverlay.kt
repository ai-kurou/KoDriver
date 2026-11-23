package kurou.kodriver.feature.splash.android

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import kurou.kodriver.feature.splash.SplashAppIcon
import kurou.kodriver.feature.splash.SplashBackdrop

/** 光が画面を横切るのにかける時間（ミリ秒）。 */
internal const val ANDROID_SPLASH_SHINE_MILLIS = 700

/** 光が終わってからオーバーレイが消えるまでの時間（ミリ秒）。 */
internal const val ANDROID_SPLASH_FADE_MILLIS = 300

/**
 * Android の OS スプラッシュが終わった直後に、メイン画面の上へ短く重ねるスプラッシュ。
 *
 * 自己ベストラップの写真と光の帯を [SplashBackdrop] で描き、光が横切ったあとにフェードアウトして
 * [onFinished] を一度通知する。表示中はタッチを消費し、背後のメイン画面を操作させない。
 * 写真の置き方は描画領域の縦横で決め、縦長のときは下端に置いて上へ溶かす。
 *
 * 表示するかどうかは [shouldShowAndroidSplashOverlay] で呼び出し側が判断する。
 */
@Composable
public fun AndroidSplashOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shineProgress = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        shineProgress.animateTo(1f, tween(ANDROID_SPLASH_SHINE_MILLIS, easing = LinearOutSlowInEasing))
        overlayAlpha.animateTo(0f, tween(ANDROID_SPLASH_FADE_MILLIS))
        currentOnFinished()
    }
    AndroidSplashOverlayContent(
        shineProgress = shineProgress.value,
        overlayAlpha = overlayAlpha.value,
        modifier = modifier,
    )
}

/** [AndroidSplashOverlay] の状態を持たない本体。[shineProgress]（0〜1）は光の位置、[overlayAlpha] は全体の不透明度。 */
@Composable
internal fun AndroidSplashOverlayContent(
    shineProgress: Float,
    overlayAlpha: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .alpha(overlayAlpha)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    }
                },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            SplashBackdrop(shineProgress = shineProgress, portrait = isPortrait(maxWidth.value, maxHeight.value)) {
                SplashAppIcon(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

/** 描画領域が縦長かどうか。幅と高さが等しいときは横長として扱う。 */
internal fun isPortrait(
    width: Float,
    height: Float,
): Boolean = height > width

/**
 * Android のスプラッシュ用オーバーレイを表示するかどうか。
 *
 * コールドスタート（プロセス起動直後の最初の Activity 生成。[isRestored] が false かつ [alreadyShown] が false）に限り、
 * アニメーションを無効にしている端末（[animatorDurationScale] が 0）では表示しない。
 *
 * @param isRestored Activity が保存状態から復元されたか（回転・プロセス再生成など）。
 * @param alreadyShown このプロセスで既にオーバーレイを表示したか。
 * @param animatorDurationScale 端末の「アニメーション縮尺」。
 */
public fun shouldShowAndroidSplashOverlay(
    isRestored: Boolean,
    alreadyShown: Boolean,
    animatorDurationScale: Float,
): Boolean = !isRestored && !alreadyShown && animatorDurationScale > 0f

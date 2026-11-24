package kurou.kodriver.feature.splash.android

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.splash.SplashAppIcon
import kurou.kodriver.feature.splash.SplashBackdrop
import kurou.kodriver.feature.splash.SplashVersionLabel
import kotlin.math.PI
import kotlin.math.sin

/** 光が画面を横切るのにかける時間（ミリ秒）。 */
internal const val ANDROID_SPLASH_SHINE_MILLIS = 700

/** 光が終わってからオーバーレイが消えるまでの時間（ミリ秒）。 */
internal const val ANDROID_SPLASH_FADE_MILLIS = 300

/**
 * OS のスプラッシュの背景色（`:app:androidApp` の `splash_background`）。オーバーレイの出だしをこの色にして、
 * OS のスプラッシュからの切り替わりで色が跳ねないようにする。
 */
public val AndroidSplashStartColor: Color = Color(0xFFBFFF00)

/**
 * OS のスプラッシュ（Android 12 以降）が中央に描くアイコン画像の一辺（dp）。288dp の枠の 60%。
 * オーバーレイのアイコンを同じ大きさにして、同じアイコンが出続けているように見せる。
 */
public const val ANDROID_SPLASH_ICON_SIZE_DP: Float = 172.8f

/** 写真の上でバージョンを読みやすくするための影。 */
private val ANDROID_SPLASH_VERSION_SHADOW =
    Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 2f), blurRadius = 6f)

/** 出だしの背景色が消えきる光の進捗。 */
internal const val ANDROID_SPLASH_START_COLOR_FADE_FRACTION = 0.3f

/** アイコンが光の進捗に合わせて最も膨らむときの拡大率の増分。 */
internal const val ANDROID_SPLASH_ICON_POP = 0.08f

/**
 * 出だしの背景色の不透明度。光が進むにつれて 1 から 0 へ消え、OS のスプラッシュの背景から写真の背景へつなぐ。
 */
internal fun startColorAlpha(shineProgress: Float): Float =
    (1f - shineProgress / ANDROID_SPLASH_START_COLOR_FADE_FRACTION).coerceIn(0f, 1f)

/** アイコンの拡大率。光の進捗に合わせて 1 から膨らみ、1 へ戻る。 */
internal fun iconScale(shineProgress: Float): Float =
    1f + ANDROID_SPLASH_ICON_POP * sin(PI.toFloat() * shineProgress.coerceIn(0f, 1f))

/**
 * Android の OS スプラッシュが終わった直後に、メイン画面の上へ短く重ねるスプラッシュ。
 *
 * 自己ベストラップの写真と光の帯を [SplashBackdrop] で描き、光が横切ったあとにフェードアウトして
 * [onFinished] を一度通知する。表示中はタッチを消費し、背後のメイン画面を操作させない。
 * 写真の置き方は描画領域の縦横で決め、縦長のときは下端に置いて上へ溶かす。
 * 出だしは OS のスプラッシュと同じ背景色・同じ位置と大きさのアイコンで、切り替わりを目立たせない。
 * 光に合わせてアイコンが少し膨らむ。
 * 下部の中央にバージョン（[appVersion]）を重ねる。OS のスプラッシュには出せないので、出だしの背景色が消えるのに合わせて現れる。
 * [started] が true になるまでは出だしの見た目のまま待つ。OS のスプラッシュが外れた後に演出を始めるために使う。
 *
 * 表示するかどうかは [shouldShowAndroidSplashOverlay] で呼び出し側が判断する。
 */
@Composable
public fun AndroidSplashOverlay(
    appVersion: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    started: Boolean = true,
) {
    val shineProgress = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(started) {
        // OS のスプラッシュが外れる前に演出が進んで見えないまま終わらないよう、合図があるまで待つ。
        if (!started) return@LaunchedEffect
        // 起動直後の重い処理でフレームが進まないうちに演出が終わらないよう、最初のフレームが描かれてから始める。
        withFrameNanos { }
        shineProgress.animateTo(1f, tween(ANDROID_SPLASH_SHINE_MILLIS, easing = LinearOutSlowInEasing))
        overlayAlpha.animateTo(0f, tween(ANDROID_SPLASH_FADE_MILLIS))
        currentOnFinished()
    }
    AndroidSplashOverlayContent(
        appVersion = appVersion,
        shineProgress = shineProgress.value,
        overlayAlpha = overlayAlpha.value,
        modifier = modifier,
    )
}

/** [AndroidSplashOverlay] の状態を持たない本体。[shineProgress]（0〜1）は光の位置、[overlayAlpha] は全体の不透明度。 */
@Composable
internal fun AndroidSplashOverlayContent(
    appVersion: String,
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
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(startColorAlpha(shineProgress))
                            .background(AndroidSplashStartColor),
                )
                SplashAppIcon(
                    modifier = Modifier.align(Alignment.Center).scale(iconScale(shineProgress)),
                    size = ANDROID_SPLASH_ICON_SIZE_DP.dp,
                )
                SplashVersionLabel(
                    appVersion = appVersion,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = KoDriverSpacing.extraLarge)
                            .alpha(1f - startColorAlpha(shineProgress)),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(shadow = ANDROID_SPLASH_VERSION_SHADOW),
                )
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

package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kurou.kodriver.feature.splash.android.AndroidSplashOverlay
import kurou.kodriver.feature.splash.android.shouldShowAndroidSplashOverlay

/**
 * Android のスプラッシュ用オーバーレイを表示するかどうか。判定は [shouldShowAndroidSplashOverlay] と同じ。
 * `:app:androidApp` が OS のスプラッシュの終了アニメーションを切り替えるために使う。
 */
public fun shouldShowAndroidSplash(
    isRestored: Boolean,
    alreadyShown: Boolean,
    animatorDurationScale: Float,
): Boolean = shouldShowAndroidSplashOverlay(isRestored, alreadyShown, animatorDurationScale)

/** OS のスプラッシュが外れた通知が届かなくても、この時間が過ぎたら演出を始める。 */
internal const val ANDROID_SPLASH_START_TIMEOUT_MILLIS = 2_000L

/**
 * Android アプリで、メイン画面の上に重ねるスプラッシュのオーバーレイ。[AppScreen] の `overlayContent` に渡す。
 *
 * [show] が true のときだけ表示し、演出が終わると取り除く。表示の可否は [shouldShowAndroidSplash] で決める。
 * アプリのテーマ（ダークテーマ・動的カラー）を使うため、テーマを適用した [AppScreen] の内側で描かれる。
 *
 * 演出は [osSplashRemoved]（OS のスプラッシュが外れた）が true になってから始める。OS のスプラッシュは
 * メイン画面の初回描画後もしばらく残るため、先に始めると見えないまま終わってしまう。通知が届かない場合に備え、
 * [ANDROID_SPLASH_START_TIMEOUT_MILLIS] 後には始める。
 */
@Composable
public fun AndroidSplashHost(
    show: Boolean,
    osSplashRemoved: Boolean,
) {
    var finished by remember { mutableStateOf(false) }
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        if (show) {
            delay(ANDROID_SPLASH_START_TIMEOUT_MILLIS)
            timedOut = true
        }
    }
    if (show && !finished) {
        AndroidSplashOverlay(onFinished = { finished = true }, started = osSplashRemoved || timedOut)
    }
}

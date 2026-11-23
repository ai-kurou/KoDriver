package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/**
 * Android アプリで、メイン画面の上に重ねるスプラッシュのオーバーレイ。[AppScreen] の `overlayContent` に渡す。
 *
 * [show] が true のときだけ表示し、演出が終わると取り除く。表示の可否は [shouldShowAndroidSplash] で決める。
 * アプリのテーマ（ダークテーマ・動的カラー）を使うため、テーマを適用した [AppScreen] の内側で描かれる。
 */
@Composable
public fun AndroidSplashHost(show: Boolean) {
    var finished by remember { mutableStateOf(false) }
    if (show && !finished) {
        AndroidSplashOverlay(onFinished = { finished = true })
    }
}

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.splash.android.AndroidSplashOverlay
import kurou.kodriver.feature.splash.android.shouldShowAndroidSplashOverlay

/**
 * Android アプリで、メイン画面 [content] の上にスプラッシュのオーバーレイを重ねるホスト。
 *
 * コールドスタートで、端末のアニメーションが有効なときだけオーバーレイを表示し、演出が終わると取り除く
 * （判定は [shouldShowAndroidSplashOverlay]）。表示の可否は最初の合成時に一度だけ決める。
 *
 * @param isRestored Activity が保存状態から復元されたか。
 * @param alreadyShown このプロセスで既にオーバーレイを表示したか。
 * @param animatorDurationScale 端末の「アニメーション縮尺」。
 */
@Composable
public fun AndroidSplashHost(
    isRestored: Boolean,
    alreadyShown: Boolean,
    animatorDurationScale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val showSplash = remember { shouldShowAndroidSplashOverlay(isRestored, alreadyShown, animatorDurationScale) }
    var finished by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        content()
        if (showSplash && !finished) {
            AndroidSplashOverlay(onFinished = { finished = true })
        }
    }
}

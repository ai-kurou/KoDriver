package kurou.kodriver

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.ExperimentalActivityApi
import androidx.activity.SystemBarStyle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CancellationException
import kurou.kodriver.presentation.AndroidSplashHost
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.shouldShowAndroidSplash

/** このプロセスで既にスプラッシュのオーバーレイを表示したか。Activity が再生成されても再表示しないために持つ。 */
private var splashOverlayShown = false

/**
 * MainActivity の Android Activity。
 */
public class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalActivityApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        super.onCreate(savedInstanceState)
        val animatorDurationScale =
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        val alreadyShown = splashOverlayShown
        splashOverlayShown = true
        val showSplash =
            shouldShowAndroidSplash(
                isRestored = savedInstanceState != null,
                alreadyShown = alreadyShown,
                animatorDurationScale = animatorDurationScale,
            )
        if (showSplash) {
            // オーバーレイが同じ見た目で引き継ぐため、OS のスプラッシュの終了アニメーション（フェード）は省いて即座に取り除く。
            splashScreen.setOnExitAnimationListener { provider -> provider.remove() }
        }
        setContent {
            AppScreen(
                overlayContent = {
                    AndroidSplashHost(show = showSplash)
                },
                backHandler = { enabled, onProgress, onBack ->
                    PredictiveBackHandler(enabled = enabled) { progress ->
                        try {
                            progress.collect { backEvent ->
                                onProgress(backEvent.progress)
                            }
                            onProgress(1f)
                            onBack()
                        } catch (e: CancellationException) {
                            onProgress(0f)
                            throw e
                        }
                    }
                },
                onDarkThemeChanged = { darkTheme ->
                    enableEdgeToEdge(
                        statusBarStyle =
                            if (darkTheme) {
                                SystemBarStyle.dark(Color.TRANSPARENT)
                            } else {
                                SystemBarStyle.light(
                                    scrim = Color.TRANSPARENT,
                                    darkScrim = Color.TRANSPARENT,
                                )
                            },
                    )
                },
            )
        }
    }
}

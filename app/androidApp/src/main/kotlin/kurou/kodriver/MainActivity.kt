package kurou.kodriver

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.ExperimentalActivityApi
import androidx.activity.SystemBarStyle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CancellationException
import kurou.kodriver.presentation.AppScreen

/**
 * MainActivity の Android Activity。
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalActivityApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        super.onCreate(savedInstanceState)
        setContent {
            AppScreen(
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
                onExit = { finish() },
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

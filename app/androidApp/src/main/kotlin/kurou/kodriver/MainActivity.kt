package kurou.kodriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.CancellationException
import kurou.kodriver.presentation.AppScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalActivityApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
            )
        }
    }
}

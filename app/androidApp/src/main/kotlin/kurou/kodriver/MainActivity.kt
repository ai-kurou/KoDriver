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
                backHandler = { enabled, onBack ->
                    PredictiveBackHandler(enabled = enabled) { progress ->
                        try {
                            progress.collect {}
                            onBack()
                        } catch (e: CancellationException) {
                            throw e
                        }
                    }
                },
                onExit = { finish() },
            )
        }
    }
}

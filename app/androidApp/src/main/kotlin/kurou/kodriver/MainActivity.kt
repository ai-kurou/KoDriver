package kurou.kodriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.readout.ReadoutContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppScreen(
                readoutContent = {
                    ReadoutContent(
                        backHandler = { enabled, onBack -> BackHandler(enabled = enabled, onBack = onBack) },
                    )
                },
            )
        }
    }
}

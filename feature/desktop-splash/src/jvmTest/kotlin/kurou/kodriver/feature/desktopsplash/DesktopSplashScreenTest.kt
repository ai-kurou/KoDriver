package kurou.kodriver.feature.desktopsplash

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DesktopSplashScreenTest {
    @Test
    fun `フェーズが変わると表示名が更新される`() =
        composeScreenshotTest {
            var step by mutableStateOf(DesktopSplashStep.INITIALIZING_MODULES)

            setContent {
                MaterialTheme {
                    DesktopSplashScreen(
                        uiState = DesktopSplashUiState(step = step),
                        modifier = Modifier,
                    )
                }
            }

            onNodeWithText("モジュールを初期化しています…").assertIsDisplayed()

            step = DesktopSplashStep.READY
            waitForIdle()

            onNodeWithText("起動が完了しました").assertIsDisplayed()
        }

    @Test
    fun `引数が変わらない再コンポーズでは表示を維持する`() =
        composeScreenshotTest {
            var tick by mutableStateOf(0)
            val uiState = DesktopSplashUiState(step = DesktopSplashStep.STARTING_SERVER)

            setContent {
                MaterialTheme {
                    @Suppress("UNUSED_VARIABLE")
                    val observed = tick
                    DesktopSplashScreen(uiState = uiState, modifier = Modifier)
                }
            }

            onNodeWithText("サーバーを起動しています…").assertIsDisplayed()

            tick++
            waitForIdle()

            onNodeWithText("サーバーを起動しています…").assertIsDisplayed()
        }
}

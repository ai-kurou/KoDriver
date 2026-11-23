package kurou.kodriver.feature.splash.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DesktopSplashScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `フェーズが変わると表示名が更新される`() {
        var step by mutableStateOf(DesktopSplashStep.INITIALIZING_MODULES)

        rule.setContent {
            MaterialTheme {
                DesktopSplashScreen(
                    uiState = DesktopSplashUiState(step = step),
                    modifier = Modifier,
                )
            }
        }

        rule.onNodeWithText("モジュールを初期化しています…").assertIsDisplayed()

        step = DesktopSplashStep.STARTING_SERVER
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        rule.onNodeWithText("サーバーを起動しています…").assertIsDisplayed()

        step = DesktopSplashStep.READY
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        rule.onNodeWithText("起動が完了しました").assertIsDisplayed()
    }

    @Test
    fun `起動完了の演出が終わると一度だけ通知する`() {
        var step by mutableStateOf(DesktopSplashStep.STARTING_SERVER)
        var finishedCount = 0

        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                DesktopSplashScreen(
                    uiState = DesktopSplashUiState(step = step),
                    onReadyAnimationFinished = { finishedCount++ },
                )
            }
        }

        rule.mainClock.advanceTimeBy(SHINE_DURATION_MILLIS * 2L)
        assertEquals(0, finishedCount)

        step = DesktopSplashStep.READY
        rule.mainClock.advanceTimeBy(SHINE_DURATION_MILLIS / 2L)
        assertEquals(0, finishedCount)

        rule.mainClock.advanceTimeBy(SHINE_DURATION_MILLIS * 2L)
        assertEquals(1, finishedCount)
    }

    @Test
    fun `引数が変わらない再コンポーズでは表示を維持する`() {
        var tick by mutableIntStateOf(0)
        val uiState = DesktopSplashUiState(step = DesktopSplashStep.STARTING_SERVER)

        rule.setContent {
            MaterialTheme {
                @Suppress("UNUSED_VARIABLE")
                val observed = tick
                DesktopSplashScreen(uiState = uiState, modifier = Modifier)
            }
        }

        rule.onNodeWithText("サーバーを起動しています…").assertIsDisplayed()

        tick++
        rule.waitForIdle()

        rule.onNodeWithText("サーバーを起動しています…").assertIsDisplayed()
    }
}

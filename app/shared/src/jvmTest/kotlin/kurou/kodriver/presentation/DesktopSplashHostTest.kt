package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DesktopSplashHostTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `初期化と完了演出が終わると一度だけ通知する`() {
        val serverGate = CompletableDeferred<Unit>()
        var finishedCount = 0

        rule.setContent {
            MaterialTheme {
                DesktopSplashHost(
                    appVersion = "1.0.0",
                    initializeModules = {},
                    startServer = { serverGate.await() },
                    onFinished = { finishedCount++ },
                )
            }
        }

        rule.onNodeWithText("サーバーを起動しています…").assertIsDisplayed()
        rule.onNodeWithText("v1.0.0").assertIsDisplayed()
        assertEquals(0, finishedCount)

        serverGate.complete(Unit)
        rule.waitForIdle()

        rule.onNodeWithText("起動が完了しました").assertIsDisplayed()
        assertEquals(1, finishedCount)
    }

    @Test
    fun `引数が変わらない再コンポーズでは再通知しない`() {
        var tick by mutableIntStateOf(0)
        var finishedCount = 0

        rule.setContent {
            MaterialTheme {
                val initializeModules: suspend () -> Unit = remember { {} }
                val startServer: suspend () -> Unit = remember { {} }

                @Suppress("UNUSED_VARIABLE")
                val observed = tick
                DesktopSplashHost(
                    appVersion = "1.0.0",
                    initializeModules = initializeModules,
                    startServer = startServer,
                    onFinished = { finishedCount++ },
                )
            }
        }

        rule.waitForIdle()

        tick++
        rule.waitForIdle()

        rule.onNodeWithText("起動が完了しました").assertIsDisplayed()
        assertEquals(1, finishedCount)
    }

    @Test
    fun `初期化が失敗するとエラーダイアログを表示し完了を通知しない`() {
        var errored: Throwable? = null
        var finishedCount = 0

        rule.setContent {
            MaterialTheme {
                DesktopSplashHost(
                    appVersion = "1.0.0",
                    initializeModules = { error("初期化に失敗しました") },
                    startServer = {},
                    onFinished = { finishedCount++ },
                    onError = { errored = it },
                )
            }
        }

        rule.waitForIdle()

        rule.onNodeWithText("起動に失敗しました").assertIsDisplayed()
        rule.onNodeWithText("初期化に失敗しました").assertIsDisplayed()
        assertEquals(0, finishedCount)

        rule.onNodeWithText("閉じる").performClick()
        rule.waitForIdle()

        assertEquals("初期化に失敗しました", errored?.message)
    }
}

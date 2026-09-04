package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopSplashHostTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `初期化中はスプラッシュを表示し完了後にコンテンツへ切り替える`() {
        val serverGate = CompletableDeferred<Unit>()
        var readyCalled = false

        rule.setContent {
            MaterialTheme {
                DesktopSplashHost(
                    initializeModules = {},
                    startServer = { serverGate.await() },
                    onReady = { readyCalled = true },
                ) {
                    Text("メイン画面")
                }
            }
        }

        rule.onNodeWithText("サーバーを起動しています…").assertIsDisplayed()
        rule.onAllNodesWithText("メイン画面").assertCountEquals(0)

        serverGate.complete(Unit)
        rule.waitForIdle()

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
        assertTrue(readyCalled)
    }

    @Test
    fun `引数が変わらない再コンポーズでもメイン画面を維持する`() {
        var tick by mutableIntStateOf(0)

        rule.setContent {
            MaterialTheme {
                val initializeModules: suspend () -> Unit = remember { {} }
                val startServer: suspend () -> Unit = remember { {} }

                @Suppress("UNUSED_VARIABLE")
                val observed = tick
                DesktopSplashHost(
                    initializeModules = initializeModules,
                    startServer = startServer,
                ) {
                    Text("メイン画面")
                }
            }
        }

        rule.onNodeWithText("メイン画面").assertIsDisplayed()

        tick++
        rule.waitForIdle()

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
    }

    @Test
    fun `初期化が失敗するとエラーダイアログを表示しコンテンツへ切り替えない`() {
        var errored: Throwable? = null

        rule.setContent {
            MaterialTheme {
                DesktopSplashHost(
                    initializeModules = { error("初期化に失敗しました") },
                    startServer = {},
                    onError = { errored = it },
                ) {
                    Text("メイン画面")
                }
            }
        }

        rule.waitForIdle()

        rule.onNodeWithText("起動に失敗しました").assertIsDisplayed()
        rule.onNodeWithText("初期化に失敗しました").assertIsDisplayed()
        rule.onAllNodesWithText("メイン画面").assertCountEquals(0)

        rule.onNodeWithText("閉じる").performClick()
        rule.waitForIdle()

        assertEquals("初期化に失敗しました", errored?.message)
    }
}

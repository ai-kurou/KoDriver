package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadoutDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと内容を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "フラッグ",
                    canNavigateBack = true,
                    onBack = {},
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("フラッグ").assertIsDisplayed()
        rule.onNodeWithText("詳細内容").assertIsDisplayed()
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "フラッグ",
                    canNavigateBack = true,
                    onBack = { backCount++ },
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻る不可の場合は戻るボタンを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "フラッグ",
                    canNavigateBack = false,
                    onBack = {},
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `scrollBehaviorを指定してもタイトルと内容を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("タイヤ温度").assertIsDisplayed()
        rule.onNodeWithText("詳細内容").assertIsDisplayed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun `下スクロールでAppBarが折りたたまれ上スクロールで再表示される`() {
        lateinit var scrollBehavior: TopAppBarScrollBehavior
        rule.setContent {
            KoDriverTheme {
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    scrollBehavior = scrollBehavior,
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        repeat(HEAVY_ITEM_COUNT) { index -> Text("項目$index") }
                    }
                }
            }
        }

        rule.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        rule.waitForIdle()
        assertTrue(scrollBehavior.state.heightOffset < 0f)

        rule.onNode(hasScrollAction()).performTouchInput { swipeDown() }
        rule.waitForIdle()
        assertEquals(0f, scrollBehavior.state.heightOffset)
    }

    @Test
    fun `rootEnabledがtrueの場合はバナーを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    rootEnabled = true,
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("「タイヤ温度」がOFFのため、読み上げられません").assertDoesNotExist()
        rule.onNodeWithText("ONにする").assertDoesNotExist()
        rule.onNodeWithText("詳細内容").assertIsDisplayed()
    }

    @Test
    fun `rootEnabledがfalseの場合はバナーを表示し内容も表示し続ける`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    rootEnabled = false,
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("「タイヤ温度」がOFFのため、読み上げられません").assertIsDisplayed()
        rule.onNodeWithText("ONにする").assertIsDisplayed()
        rule.onNodeWithText("詳細内容").assertIsDisplayed()
    }

    @Test
    fun `ONにするをタップするとonEnableRootが呼ばれる`() {
        var enableCount = 0
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    rootEnabled = false,
                    onEnableRoot = { enableCount++ },
                ) {
                    Text("詳細内容")
                }
            }
        }

        rule.onNodeWithText("ONにする").performClick()

        assertEquals(1, enableCount)
    }

    @Test
    fun `rootEnabledがfalseでも内容の操作は可能`() {
        var clickCount = 0
        rule.setContent {
            KoDriverTheme {
                ReadoutDetailPane(
                    title = "タイヤ温度",
                    canNavigateBack = true,
                    onBack = {},
                    rootEnabled = false,
                ) {
                    TextButton(onClick = { clickCount++ }) { Text("試聴") }
                }
            }
        }

        rule.onNodeWithText("試聴").performClick()

        assertEquals(1, clickCount)
    }

    private companion object {
        const val HEAVY_ITEM_COUNT = 50
    }
}

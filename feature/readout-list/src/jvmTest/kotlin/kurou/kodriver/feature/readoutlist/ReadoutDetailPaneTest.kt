package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

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
}

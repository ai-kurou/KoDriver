package kurou.kodriver.feature.readoutlist

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import kotlin.test.assertEquals

class ReadoutDetailPaneTest {
    @Test
    fun `タイトルと内容を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("フラッグ").assertIsDisplayed()
            onNodeWithText("詳細内容").assertIsDisplayed()
        }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() =
        composeScreenshotTest {
            var backCount = 0
            setContent {
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

            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, backCount)
        }

    @Test
    fun `戻る不可の場合は戻るボタンを表示しない`() =
        composeScreenshotTest {
            setContent {
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

            onNode(hasContentDescription("戻る")).assertDoesNotExist()
        }
}

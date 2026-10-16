package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test

class KoDriverHintRowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `末尾テキストを表示しヘルプボタンでシートを開く`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverHintRow(
                    label = "読み上げ優先度",
                    helpContentDescription = "優先度の説明",
                    trailingText = "上から順",
                ) {
                    KoDriverHintSheetRow(
                        icon = Icons.Default.Info,
                        title = "並べ替え",
                        description = "ドラッグで並べ替えます",
                    )
                }
            }
        }

        rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
        rule.onNodeWithText("上から順").assertIsDisplayed()
        rule.onNodeWithText("並べ替え").assertDoesNotExist()
        rule.onNodeWithContentDescription("優先度の説明").performClick()
        rule.onNodeWithText("並べ替え").assertIsDisplayed()
        rule.onNodeWithText("ドラッグで並べ替えます").assertIsDisplayed()
    }

    @Test
    fun `末尾テキストを省略してシートを開く`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverHintRow(
                    label = "手動読み上げ",
                    helpContentDescription = "操作の説明",
                ) {
                    Text("セットを読み上げます")
                }
            }
        }

        rule.onNodeWithText("手動読み上げ").assertIsDisplayed()
        rule.onNodeWithText("セットを読み上げます").assertDoesNotExist()
        rule.onNodeWithContentDescription("操作の説明").performClick()
        rule.onNodeWithText("セットを読み上げます").assertIsDisplayed()
        rule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .performSemanticsAction(SemanticsActions.Dismiss)
        rule.onNodeWithText("セットを読み上げます").assertDoesNotExist()
        rule.onNodeWithContentDescription("操作の説明").performClick()
        rule.onNodeWithText("セットを読み上げます").assertIsDisplayed()
    }
}

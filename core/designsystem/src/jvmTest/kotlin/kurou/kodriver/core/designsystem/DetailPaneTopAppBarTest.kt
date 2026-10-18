package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DetailPaneTopAppBarTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `戻るボタンの説明文をdesignsystemの文字列リソースから表示する`() {
        rule.setContent {
            KoDriverTheme {
                DetailPaneTopAppBar(
                    title = "ライセンス",
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNode(hasContentDescription("戻る")).assertIsDisplayed()
    }

    @Test
    fun `20文字のセット名は戻るボタンと操作があっても1行で末尾を省略する`() {
        assertTitleLayout("WWWWWWWWWWWWWWWWWWWW")
    }

    @Test
    fun `短いタイトルは1行で省略せず表示する`() {
        assertTitleLayout("車両接近")
    }

    private fun assertTitleLayout(title: String) {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                    DetailPaneTopAppBar(
                        title = title,
                        canNavigateBack = true,
                        onBack = {},
                        actions = { Text("保存") },
                    )
                }
            }
        }

        val layoutResults = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText(title).assertIsDisplayed().performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
            it(layoutResults)
        }
        assertEquals(1, layoutResults.single().lineCount)
        assertEquals(1, layoutResults.single().layoutInput.maxLines)
        assertEquals(TextOverflow.Ellipsis, layoutResults.single().layoutInput.overflow)
        rule.onNode(hasContentDescription("戻る")).assertIsDisplayed()
        rule.onNodeWithText("保存").assertIsDisplayed()
    }
}

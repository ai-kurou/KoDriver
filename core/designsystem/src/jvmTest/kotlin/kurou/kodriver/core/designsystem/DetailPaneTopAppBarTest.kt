package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test

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
}

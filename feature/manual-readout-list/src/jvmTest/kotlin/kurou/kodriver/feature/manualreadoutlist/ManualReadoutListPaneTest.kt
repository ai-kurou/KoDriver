package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `セットがまだない空状態を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPane()
            }
        }

        rule.onNodeWithText("手動読み上げのセットはまだありません").assertIsDisplayed()
    }
}

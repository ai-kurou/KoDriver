package kurou.kodriver.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class KoDriverHelpSheetImageContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文を表示する`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverHelpSheetImageContent(
                    description = "閾値の説明です",
                    painter = ColorPainter(Color.Red),
                )
            }
        }

        rule.onNodeWithText("閾値の説明です").assertIsDisplayed()
    }
}

package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class Gt7Ps5ReadoutRemainingFuelDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `燃料残量の説明を表示する`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
            }
        }

        rule.onNodeWithText("GT7のテレメトリから燃料残量を取得し", substring = true)
            .assertIsDisplayed()
    }
}

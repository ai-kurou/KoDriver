package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutVehicleApproachDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPane()
            }
        }

        rule.onNodeWithText("周囲の車両が接近した際に音声でお知らせします。").assertIsDisplayed()
    }
}

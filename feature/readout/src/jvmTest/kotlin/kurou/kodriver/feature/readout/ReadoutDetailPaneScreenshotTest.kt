package kurou.kodriver.feature.readout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ReadoutDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `戻るボタンあり`() {
        rule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ReadoutDetailPane(canNavigateBack = true, onBack = {}, content = {})
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `戻るボタンなし`() {
        rule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ReadoutDetailPane(canNavigateBack = false, onBack = {}, content = {})
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}

package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.presentation.captureRoboImage
import org.junit.Rule
import org.junit.Test

class ReadoutListPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    ReadoutListPane(onItemClick = {})
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}

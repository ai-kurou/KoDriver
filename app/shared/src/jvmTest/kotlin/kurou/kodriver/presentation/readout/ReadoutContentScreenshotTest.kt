package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.presentation.captureRoboImage
import kurou.kodriver.presentation.twoPaneDirective
import org.junit.Rule
import org.junit.Test

class ReadoutContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `一覧と詳細の同時表示`() {
        rule.setContent {
            MaterialTheme {
                ReadoutContent(
                    modifier = Modifier.requiredSize(840.dp, 640.dp),
                    scaffoldDirective = twoPaneDirective,
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }
}

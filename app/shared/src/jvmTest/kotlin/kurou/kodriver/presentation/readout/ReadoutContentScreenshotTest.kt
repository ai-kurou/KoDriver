package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ReadoutContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private val twoPaneDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = 2,
        horizontalPartitionSpacerSize = 16.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

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

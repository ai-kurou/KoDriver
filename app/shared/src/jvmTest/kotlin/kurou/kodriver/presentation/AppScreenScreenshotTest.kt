package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.presentation.readout.ReadoutContent
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AppScreenScreenshotTest {

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
    fun `読み上げタブ`() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = { ReadoutContent(scaffoldDirective = twoPaneDirective) })
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `その他タブ`() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = { ReadoutContent(scaffoldDirective = twoPaneDirective) })
            }
        }
        rule.onNodeWithText("その他").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage()
    }
}

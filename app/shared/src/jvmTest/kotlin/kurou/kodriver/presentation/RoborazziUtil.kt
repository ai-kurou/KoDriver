package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage

private val defaultOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.005f,
            ),
    )

internal fun SemanticsNodeInteraction.captureRoboImage() = captureRoboImage(roborazziOptions = defaultOptions)

// フルHDタブレット相当のウィンドウサイズ。デフォルト(1024x768)では縦に長いlistPaneなどの
// 内容を1枚で撮影しきれないため、撮影可能な上限を引き上げる。
private const val SCREENSHOT_TEST_WINDOW_WIDTH_DP = 1920
private const val SCREENSHOT_TEST_WINDOW_HEIGHT_DP = 1080

@OptIn(ExperimentalTestApi::class)
internal fun composeScreenshotTest(block: suspend DesktopComposeUiTest.() -> Unit) {
    runDesktopComposeUiTest(
        width = SCREENSHOT_TEST_WINDOW_WIDTH_DP,
        height = SCREENSHOT_TEST_WINDOW_HEIGHT_DP,
        block = block,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal val twoPaneDirective =
    PaneScaffoldDirective(
        maxHorizontalPartitions = 2,
        horizontalPartitionSpacerSize = 16.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal val singlePaneDirective =
    PaneScaffoldDirective(
        maxHorizontalPartitions = 1,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

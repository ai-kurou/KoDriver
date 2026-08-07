package kurou.kodriver.buildlogic.screenshottest

import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage

val defaultRoborazziOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.005f,
            ),
    )

fun SemanticsNodeInteraction.captureRoboImage() = captureRoboImage(roborazziOptions = defaultRoborazziOptions)

// フルHDタブレット相当のウィンドウサイズ。デフォルト(1024x768)では縦に長いlistPaneなどの
// 内容を1枚で撮影しきれないため、撮影可能な上限を引き上げる。
private const val SCREENSHOT_TEST_WINDOW_WIDTH_DP = 1920
private const val SCREENSHOT_TEST_WINDOW_HEIGHT_DP = 1080

@OptIn(ExperimentalTestApi::class)
fun composeScreenshotTest(block: suspend DesktopComposeUiTest.() -> Unit) {
    runDesktopComposeUiTest(
        width = SCREENSHOT_TEST_WINDOW_WIDTH_DP,
        height = SCREENSHOT_TEST_WINDOW_HEIGHT_DP,
        block = block,
    )
}

package kurou.kodriver.feature.desktopsplash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class DesktopSplashScreenScreenshotTest {
    @Test
    fun `モジュール初期化中`() =
        composeScreenshotTest {
            capture(DesktopSplashStep.INITIALIZING_MODULES)
        }

    @Test
    fun `サーバー起動中`() =
        composeScreenshotTest {
            capture(DesktopSplashStep.STARTING_SERVER)
        }

    @Test
    fun `起動完了`() =
        composeScreenshotTest {
            capture(DesktopSplashStep.READY)
        }

    private fun DesktopComposeUiTest.capture(step: DesktopSplashStep) {
        setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(600.dp, 500.dp)) {
                    DesktopSplashScreen(uiState = DesktopSplashUiState(step = step))
                }
            }
        }
        onRoot().captureRoboImage()
    }
}

package kurou.kodriver.feature.desktopsplash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class DesktopSplashScreenScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `モジュール初期化中`() {
        capture(DesktopSplashStep.INITIALIZING_MODULES)
    }

    @Test
    fun `サーバー起動中`() {
        capture(DesktopSplashStep.STARTING_SERVER)
    }

    @Test
    fun `起動完了`() {
        capture(DesktopSplashStep.READY)
    }

    private fun capture(step: DesktopSplashStep) {
        rule.setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(600.dp, 500.dp)) {
                    DesktopSplashScreen(uiState = DesktopSplashUiState(step = step))
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}

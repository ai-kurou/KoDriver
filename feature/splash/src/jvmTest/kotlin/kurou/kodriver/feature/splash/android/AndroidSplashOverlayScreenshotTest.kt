package kurou.kodriver.feature.splash.android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

/** 縦向き（写真を下端に置いて上へ溶かす）はデスクトップのスプラッシュに存在しない表示のため、Android 側で固定する。 */
class AndroidSplashOverlayScreenshotTest {
    @Test
    fun `縦向き_開始直後は開始色が残る`() =
        composeScreenshotTest {
            capture(width = 400.dp, height = 800.dp, shineProgress = 0.1f)
        }

    @Test
    fun `縦向き_光が横切る途中`() =
        composeScreenshotTest {
            capture(width = 400.dp, height = 800.dp, shineProgress = 0.5f)
        }

    @Test
    fun `縦向き_光が通り過ぎたあと`() =
        composeScreenshotTest {
            capture(width = 400.dp, height = 800.dp, shineProgress = 1f)
        }

    @Test
    fun `横向き_光が横切る途中`() =
        composeScreenshotTest {
            capture(width = 800.dp, height = 400.dp, shineProgress = 0.5f)
        }

    private fun DesktopComposeUiTest.capture(
        width: Dp,
        height: Dp,
        shineProgress: Float,
    ) {
        setContent {
            KoDriverTheme {
                Box(modifier = Modifier.requiredSize(width, height)) {
                    AndroidSplashOverlayContent(shineProgress = shineProgress, overlayAlpha = 1f)
                }
            }
        }
        onRoot().captureRoboImage()
    }
}

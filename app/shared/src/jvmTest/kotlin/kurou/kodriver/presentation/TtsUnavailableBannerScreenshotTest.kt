package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Test

class TtsUnavailableBannerScreenshotTest {
    @Test
    fun `利用不可理由ごとのバナー ライトテーマ`() = composeScreenshotTest { captureReasons(darkTheme = false) }

    @Test
    fun `利用不可理由ごとのバナー ダークテーマ`() = composeScreenshotTest { captureReasons(darkTheme = true) }

    @Test
    fun `スマホ幅でIP未設定とTTS利用不可を同時表示`() =
        composeScreenshotTest {
            captureScreen(NavigationSuiteType.NavigationBar, width = 360, darkTheme = false)
        }

    @Test
    fun `デスクトップ幅でIP未設定とTTS利用不可を同時表示 ダークテーマ`() =
        composeScreenshotTest {
            captureScreen(NavigationSuiteType.NavigationRail, width = 720, darkTheme = true)
        }

    private fun DesktopComposeUiTest.captureReasons(darkTheme: Boolean) {
        setContent {
            AppTheme(darkTheme = darkTheme) {
                Surface {
                    Column(modifier = Modifier.requiredWidth(360.dp)) {
                        TtsUnavailableGuidance.entries.forEach { reason ->
                            TtsUnavailableBanner(reason = reason, onClick = {})
                        }
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }

    private fun DesktopComposeUiTest.captureScreen(
        layoutType: NavigationSuiteType,
        width: Int,
        darkTheme: Boolean,
    ) {
        setContent {
            Box(modifier = Modifier.requiredSize(width.dp, 640.dp)) {
                AppScreenContent(
                    darkTheme = darkTheme,
                    layoutType = layoutType,
                    bannerUiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "ゲーム機・SimHubへ接続するIPアドレスが未設定です",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = true,
                        ),
                    onBannerTap = {},
                    ttsUnavailableGuidance = TtsUnavailableGuidance.WindowsSpeechUnavailable,
                )
            }
        }
        onRoot().captureRoboImage()
    }
}

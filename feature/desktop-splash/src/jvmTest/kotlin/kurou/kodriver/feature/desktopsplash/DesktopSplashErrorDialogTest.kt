package kurou.kodriver.feature.desktopsplash

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertTrue

class DesktopSplashErrorDialogTest {
    @Test
    fun `メッセージを表示し閉じるボタンでonConfirmを呼ぶ`() =
        composeScreenshotTest {
            var confirmed = false

            setContent {
                MaterialTheme {
                    DesktopSplashErrorDialog(
                        message = "初期化に失敗しました",
                        onConfirm = { confirmed = true },
                    )
                }
            }

            onNodeWithText("起動に失敗しました").assertIsDisplayed()
            onNodeWithText("初期化に失敗しました").assertIsDisplayed()

            onNodeWithText("閉じる").performClick()
            waitForIdle()

            assertTrue(confirmed)
        }
}

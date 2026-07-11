package kurou.kodriver.feature.desktopsplash

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class DesktopSplashErrorDialogTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `メッセージを表示し閉じるボタンでonConfirmを呼ぶ`() {
        var confirmed = false

        rule.setContent {
            MaterialTheme {
                DesktopSplashErrorDialog(
                    message = "初期化に失敗しました",
                    onConfirm = { confirmed = true },
                )
            }
        }

        rule.onNodeWithText("起動に失敗しました").assertIsDisplayed()
        rule.onNodeWithText("初期化に失敗しました").assertIsDisplayed()

        rule.onNodeWithText("閉じる").performClick()
        rule.waitForIdle()

        assertTrue(confirmed)
    }
}

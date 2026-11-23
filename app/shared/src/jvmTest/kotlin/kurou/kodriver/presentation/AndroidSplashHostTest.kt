package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AndroidSplashHostTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `コールドスタートではメイン画面の上にスプラッシュを重ね演出後に取り除く`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                AndroidSplashHost(isRestored = false, alreadyShown = false, animatorDurationScale = 1f) {
                    Text("メイン画面")
                }
            }
        }

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
        rule.onNodeWithContentDescription("KoDriver").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
        rule.onNodeWithContentDescription("KoDriver").assertDoesNotExist()
    }

    @Test
    fun `表示済みのときはスプラッシュを重ねない`() {
        rule.setContent {
            MaterialTheme {
                AndroidSplashHost(isRestored = false, alreadyShown = true, animatorDurationScale = 1f) {
                    Text("メイン画面")
                }
            }
        }

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
        rule.onNodeWithContentDescription("KoDriver").assertDoesNotExist()
    }
}

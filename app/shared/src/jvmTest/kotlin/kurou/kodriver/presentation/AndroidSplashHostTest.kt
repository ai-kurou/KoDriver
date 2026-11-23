package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidSplashHostTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `コールドスタートではスプラッシュを表示し演出後に取り除く`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                Text("メイン画面")
                AndroidSplashHost(show = true)
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
    fun `表示しないときはスプラッシュを重ねない`() {
        rule.setContent {
            MaterialTheme {
                Text("メイン画面")
                AndroidSplashHost(show = false)
            }
        }

        rule.onNodeWithText("メイン画面").assertIsDisplayed()
        rule.onNodeWithContentDescription("KoDriver").assertDoesNotExist()
    }

    @Test
    fun `表示の可否はオーバーレイの判定と一致する`() {
        assertTrue(shouldShowAndroidSplash(isRestored = false, alreadyShown = false, animatorDurationScale = 1f))
        assertFalse(shouldShowAndroidSplash(isRestored = true, alreadyShown = false, animatorDurationScale = 1f))
    }
}

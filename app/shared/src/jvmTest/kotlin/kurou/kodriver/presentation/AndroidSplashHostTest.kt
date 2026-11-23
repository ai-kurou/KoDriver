package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
                AndroidSplashHost(show = true, osSplashRemoved = true)
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
    fun `OSのスプラッシュが外れる前は演出を始めず外れたら始める`() {
        rule.mainClock.autoAdvance = false
        var removed by mutableStateOf(false)
        rule.setContent {
            MaterialTheme {
                AndroidSplashHost(show = true, osSplashRemoved = removed)
            }
        }

        rule.mainClock.advanceTimeBy(1_500)
        rule.onNodeWithContentDescription("KoDriver").assertIsDisplayed()

        removed = true
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()

        rule.onNodeWithContentDescription("KoDriver").assertDoesNotExist()
    }

    @Test
    fun `外れた通知が届かなくても一定時間後に演出を始めて取り除く`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                AndroidSplashHost(show = true, osSplashRemoved = false)
            }
        }

        rule.mainClock.advanceTimeBy(ANDROID_SPLASH_START_TIMEOUT_MILLIS - 100)
        rule.onNodeWithContentDescription("KoDriver").assertIsDisplayed()

        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()

        rule.onNodeWithContentDescription("KoDriver").assertDoesNotExist()
    }

    @Test
    fun `表示しないときはスプラッシュを重ねない`() {
        rule.setContent {
            MaterialTheme {
                Text("メイン画面")
                AndroidSplashHost(show = false, osSplashRemoved = true)
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

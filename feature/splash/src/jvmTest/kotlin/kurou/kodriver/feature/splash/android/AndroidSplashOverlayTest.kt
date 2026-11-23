package kurou.kodriver.feature.splash.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidSplashOverlayTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `コールドスタートでアニメーションが有効なときは表示する`() {
        assertTrue(shouldShowAndroidSplashOverlay(isRestored = false, alreadyShown = false, animatorDurationScale = 1f))
    }

    @Test
    fun `保存状態から復元されたときは表示しない`() {
        assertFalse(shouldShowAndroidSplashOverlay(isRestored = true, alreadyShown = false, animatorDurationScale = 1f))
    }

    @Test
    fun `このプロセスで表示済みのときは表示しない`() {
        assertFalse(shouldShowAndroidSplashOverlay(isRestored = false, alreadyShown = true, animatorDurationScale = 1f))
    }

    @Test
    fun `アニメーション縮尺が0のときは表示しない`() {
        assertFalse(
            shouldShowAndroidSplashOverlay(isRestored = false, alreadyShown = false, animatorDurationScale = 0f),
        )
    }

    @Test
    fun `縦長のときだけ縦向きとして扱う`() {
        assertTrue(isPortrait(width = 400f, height = 800f))
        assertFalse(isPortrait(width = 800f, height = 400f))
        assertFalse(isPortrait(width = 600f, height = 600f))
    }

    @Test
    fun `演出が終わると一度だけ終了を通知する`() {
        var finishedCount = 0
        rule.mainClock.autoAdvance = false
        rule.setContent { MaterialTheme { AndroidSplashOverlay(onFinished = { finishedCount++ }) } }

        rule.onNodeWithContentDescription("KoDriver").assertExists()
        assertEquals(0, finishedCount)

        rule.mainClock.advanceTimeBy((ANDROID_SPLASH_SHINE_MILLIS + ANDROID_SPLASH_FADE_MILLIS + 100).toLong())
        rule.waitForIdle()

        assertEquals(1, finishedCount)
    }

    @Test
    fun `表示中は背後の操作にタッチを渡さない`() {
        var clicked = false
        rule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("背後", modifier = Modifier.fillMaxSize().clickable { clicked = true })
                    AndroidSplashOverlayContent(shineProgress = 0f, overlayAlpha = 1f)
                }
            }
        }

        rule.onNodeWithContentDescription("KoDriver").performClick()
        rule.onNodeWithText("背後", useUnmergedTree = true).assertExists()
        assertFalse(clicked)
    }
}

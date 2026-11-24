package kurou.kodriver.feature.splash.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
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
        rule.setContent {
            MaterialTheme { AndroidSplashOverlay(appVersion = "1.0.0", onFinished = { finishedCount++ }) }
        }

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
                    AndroidSplashOverlayContent(appVersion = "1.0.0", shineProgress = 0f, overlayAlpha = 1f)
                }
            }
        }

        rule.onNodeWithContentDescription("KoDriver").performClick()
        rule.onNodeWithText("背後", useUnmergedTree = true).assertExists()
        assertFalse(clicked)
    }

    @Test
    fun `出だしの背景色は光が進むと消える`() {
        assertEquals(1f, startColorAlpha(0f))
        assertEquals(0.5f, startColorAlpha(ANDROID_SPLASH_START_COLOR_FADE_FRACTION / 2f), 0.0001f)
        assertEquals(0f, startColorAlpha(ANDROID_SPLASH_START_COLOR_FADE_FRACTION))
        assertEquals(0f, startColorAlpha(1f))
    }

    @Test
    fun `アイコンは光の中ほどで最も膨らみ両端で元の大きさに戻る`() {
        assertEquals(1f, iconScale(0f), 0.0001f)
        assertEquals(1f + ANDROID_SPLASH_ICON_POP, iconScale(0.5f), 0.0001f)
        assertEquals(1f, iconScale(1f), 0.0001f)
        assertEquals(1f, iconScale(2f), 0.0001f)
    }

    @Test
    fun `光が進むとバージョンを表示する`() {
        rule.setContent {
            MaterialTheme { AndroidSplashOverlayContent(appVersion = "1.2.3", shineProgress = 1f, overlayAlpha = 1f) }
        }

        rule.onNodeWithText("v1.2.3").assertIsDisplayed()
    }
}

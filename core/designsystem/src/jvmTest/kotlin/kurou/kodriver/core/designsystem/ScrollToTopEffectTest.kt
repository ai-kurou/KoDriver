package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ScrollToTopEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `初期値0では先頭スクロールを要求しない`() {
        var scrollToTopCount = 0

        composeRule.setContent {
            ScrollToTopEffect(scrollToTopRequest = 0) {
                scrollToTopCount++
            }
        }

        composeRule.waitForIdle()

        assertEquals(0, scrollToTopCount)
    }

    @Test
    fun `正の値に変化すると先頭スクロールを要求する`() {
        var scrollToTopRequest by mutableIntStateOf(0)
        var scrollToTopCount = 0

        composeRule.setContent {
            ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
                scrollToTopCount++
            }
        }

        composeRule.waitForIdle()
        scrollToTopRequest = 1
        composeRule.waitForIdle()

        assertEquals(1, scrollToTopCount)
    }

    @Test
    fun `再度異なる正の値に変化すると再度先頭スクロールを要求する`() {
        var scrollToTopRequest by mutableIntStateOf(1)
        var scrollToTopCount = 0

        composeRule.setContent {
            ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
                scrollToTopCount++
            }
        }

        composeRule.waitForIdle()
        scrollToTopRequest = 2
        composeRule.waitForIdle()

        assertEquals(2, scrollToTopCount)
    }
}

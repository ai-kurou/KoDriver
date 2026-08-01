@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KeepScreenOnEffectAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `enabledがtrueならViewのkeepScreenOnをtrueにする`() {
        val enabled = mutableStateOf(true)
        val view = setKeepScreenOnContent(enabled)

        assertTrue(view.keepScreenOn)
    }

    @Test
    fun `enabledがfalseならViewのkeepScreenOnをfalseにする`() {
        val enabled = mutableStateOf(false)
        val view = setKeepScreenOnContent(enabled)

        assertFalse(view.keepScreenOn)
    }

    @Test
    fun `compositionから外れるとViewのkeepScreenOnをfalseに戻す`() {
        val included = mutableStateOf(true)
        val view = setDisposableKeepScreenOnContent(included)

        included.value = false
        composeRule.waitForIdle()

        assertFalse(view.keepScreenOn)
    }

    private fun setKeepScreenOnContent(enabled: androidx.compose.runtime.State<Boolean>): View {
        var view: View? = null
        composeRule.setContent {
            view = LocalView.current
            KeepScreenOnEffect(enabled = enabled.value)
        }
        composeRule.waitForIdle()
        return view ?: error("LocalView was not captured.")
    }

    private fun setDisposableKeepScreenOnContent(included: androidx.compose.runtime.State<Boolean>): View {
        var view: View? = null
        composeRule.setContent {
            view = LocalView.current
            if (included.value) {
                KeepScreenOnEffect(enabled = true)
            }
        }
        composeRule.waitForIdle()
        return view ?: error("LocalView was not captured.")
    }
}

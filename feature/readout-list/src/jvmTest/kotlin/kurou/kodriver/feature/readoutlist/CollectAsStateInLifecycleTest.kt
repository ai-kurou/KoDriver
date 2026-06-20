@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.readoutlist

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CollectAsStateInLifecycleTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `初期値がStateに反映される`() {
        val flow = MutableStateFlow("initial")
        var actual: String? = null

        rule.setContent {
            val state by flow.collectAsStateInLifecycle()
            actual = state
        }

        rule.waitForIdle()
        assertEquals("initial", actual)
    }

    @Test
    fun `flowの値を更新するとStateが更新される`() {
        val flow = MutableStateFlow("first")
        var actual: String? = null

        rule.setContent {
            val state by flow.collectAsStateInLifecycle()
            actual = state
        }

        rule.waitForIdle()
        flow.value = "second"
        rule.waitForIdle()

        assertEquals("second", actual)
    }
}

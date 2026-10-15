package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

class TextInsertStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `同じ文字列も別要求になり古い要求の消費では最新要求を消さない`() {
        val state = TextInsertState()
        assertNull(state.request)
        state.insert("挿入")
        val first = assertNotNull(state.request)
        state.insert("挿入")
        val second = assertNotNull(state.request)

        assertNotSame(first, second)
        assertEquals("挿入", first.text)
        assertEquals("挿入", second.text)
        state.consume(first)
        assertSame(second, state.request)
        state.consume(second)
        assertNull(state.request)
    }

    @Test
    fun `再コンポーズでも同じ状態と未消費の要求を保持する`() {
        var label by mutableStateOf("初期値")
        var observedLabel = ""
        lateinit var state: TextInsertState
        rule.setContent {
            observedLabel = label
            state = rememberTextInsertState()
        }
        rule.waitForIdle()
        val initial = state
        rule.runOnIdle {
            state.insert("挿入")
            label = "更新値"
        }
        rule.waitForIdle()

        assertEquals("更新値", observedLabel)
        assertSame(initial, state)
        assertEquals("挿入", assertNotNull(state.request).text)
    }
}

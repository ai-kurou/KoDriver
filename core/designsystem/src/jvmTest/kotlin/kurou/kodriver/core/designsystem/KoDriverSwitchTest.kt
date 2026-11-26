package kurou.kodriver.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverSwitchTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タップするとハプティックを1回発生させて切り替え後の値でonCheckedChangeを1回呼ぶ`() {
        val haptic = RecordingHapticFeedback()
        val received = mutableListOf<Boolean>()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverSwitch(
                        checked = false,
                        onCheckedChange = { received += it },
                    )
                }
            }
        }

        rule.onNode(isToggleable()).performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        assertEquals(listOf(true), received)
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val performedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performedTypes += hapticFeedbackType
        }
    }
}

package kurou.kodriver.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverRadioRowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `選択中の行は選択状態として表示する`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverRadioRow(selected = true, label = "ダークモード", onClick = {})
            }
        }

        rule.onNode(isSelectable() and hasText("ダークモード")).assertIsSelected()
    }

    @Test
    fun `タップするとハプティックを1回発生させてonClickを1回呼ぶ`() {
        val haptic = RecordingHapticFeedback()
        var clickCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverRadioRow(
                        selected = false,
                        label = "ライトモード",
                        onClick = { clickCount++ },
                    )
                }
            }
        }

        rule.onNodeWithText("ライトモード").performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        assertEquals(1, clickCount)
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val performedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performedTypes += hapticFeedbackType
        }
    }
}

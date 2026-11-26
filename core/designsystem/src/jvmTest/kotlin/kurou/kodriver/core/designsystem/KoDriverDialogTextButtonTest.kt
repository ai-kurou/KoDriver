package kurou.kodriver.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverDialogTextButtonTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タップするとハプティックを1回発生させてonClickを1回呼ぶ`() {
        val haptic = RecordingHapticFeedback()
        var clickCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverDialogTextButton(
                        text = "OK",
                        onClick = { clickCount++ },
                    )
                }
            }
        }

        rule.onNodeWithText("OK").performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        assertEquals(1, clickCount)
    }

    @Test
    fun `無効化されたボタンはタップしてもハプティックとonClickを発生させない`() {
        val haptic = RecordingHapticFeedback()
        var clickCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverDialogTextButton(
                        text = "OK",
                        onClick = { clickCount++ },
                        enabled = false,
                    )
                }
            }
        }

        rule.onNodeWithText("OK").assertIsNotEnabled()
        rule.onNodeWithText("OK").performClick()

        assertEquals(emptyList(), haptic.performedTypes)
        assertEquals(0, clickCount)
    }

    @Test
    fun `hapticFeedback引数を渡すとその値で振動させる`() {
        val haptic = RecordingHapticFeedback()
        var clickCount = 0
        rule.setContent {
            KoDriverTheme {
                KoDriverDialogTextButton(
                    text = "キャンセル",
                    onClick = { clickCount++ },
                    hapticFeedback = haptic,
                )
            }
        }

        rule.onNodeWithText("キャンセル").performClick()

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

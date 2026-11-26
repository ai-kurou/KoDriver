package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class KoDriverAlertDialogTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `すべてのスロットを表示し振動は発生させない`() {
        val haptic = RecordingHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverAlertDialog(
                        onDismissRequest = {},
                        confirmButton = { Text("確認") },
                        dismissButton = { Text("キャンセル") },
                        title = { Text("タイトル") },
                        text = { Text("本文") },
                    )
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("確認").assertIsDisplayed()
        rule.onNodeWithText("キャンセル").assertIsDisplayed()
        rule.onNodeWithText("タイトル").assertIsDisplayed()
        rule.onNodeWithText("本文").assertIsDisplayed()
        assertEquals(emptyList(), haptic.performedTypes)
    }

    @Test
    fun `すべてのスロットで呼び出し元で読んだハプティックを使う`() {
        val haptic = RecordingHapticFeedback()
        val slotHaptics = mutableMapOf<String, HapticFeedback>()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverAlertDialog(
                        onDismissRequest = {},
                        confirmButton = { slotHaptics["confirmButton"] = LocalHapticFeedback.current },
                        dismissButton = { slotHaptics["dismissButton"] = LocalHapticFeedback.current },
                        title = { slotHaptics["title"] = LocalHapticFeedback.current },
                        text = { slotHaptics["text"] = LocalHapticFeedback.current },
                    )
                }
            }
        }
        rule.waitForIdle()

        assertEquals(setOf("confirmButton", "dismissButton", "title", "text"), slotHaptics.keys)
        slotHaptics.values.forEach { assertSame(haptic, it) }
    }

    @Test
    fun `省略できるスロットがなくても表示できる`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverAlertDialog(onDismissRequest = {}, confirmButton = { Text("確認") })
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("確認").assertIsDisplayed()
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val performedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performedTypes += hapticFeedbackType
        }
    }
}

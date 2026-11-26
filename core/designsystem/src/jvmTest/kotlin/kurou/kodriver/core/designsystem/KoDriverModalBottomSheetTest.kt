package kurou.kodriver.core.designsystem

import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
class KoDriverModalBottomSheetTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `シートの内容を表示し振動は発生させない`() {
        val haptic = RecordingHapticFeedback()
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverModalBottomSheet(onDismissRequest = {}) {
                        Text("シートの内容")
                    }
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("シートの内容").assertIsDisplayed()
        assertEquals(emptyList(), haptic.performedTypes)
    }

    @Test
    fun `シート内でも呼び出し元で読んだハプティックを使う`() {
        val haptic = RecordingHapticFeedback()
        var sheetHaptic: HapticFeedback? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverModalBottomSheet(onDismissRequest = {}) {
                        sheetHaptic = LocalHapticFeedback.current
                    }
                }
            }
        }
        rule.waitForIdle()

        assertSame(haptic, sheetHaptic)
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val performedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performedTypes += hapticFeedbackType
        }
    }
}

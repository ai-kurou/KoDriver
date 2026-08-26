package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private class FakeHapticFeedback : HapticFeedback {
    val performedTypes = mutableListOf<HapticFeedbackType>()

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        performedTypes += hapticFeedbackType
    }
}

class HelpIconButtonTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `初期状態ではヘルプ内容は表示されない`() {
        rule.setContent {
            KoDriverTheme {
                HelpIconButton(
                    contentDescription = "閾値の説明を表示",
                    sheetContent = { Text("ヘルプの説明文") },
                )
            }
        }

        rule.onNodeWithText("ヘルプの説明文").assertDoesNotExist()
    }

    @Test
    fun `アイコンをタップするとヘルプ内容が表示される`() {
        rule.setContent {
            KoDriverTheme {
                HelpIconButton(
                    contentDescription = "閾値の説明を表示",
                    sheetContent = { Text("ヘルプの説明文") },
                )
            }
        }

        rule.onNode(hasContentDescription("閾値の説明を表示")).performClick()

        rule.onNodeWithText("ヘルプの説明文").assertIsDisplayed()
    }

    @Test
    fun `アイコンをタップするとハプティックフィードバックを発生させる`() {
        val haptic = FakeHapticFeedback()
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                    HelpIconButton(
                        contentDescription = "閾値の説明を表示",
                        sheetContent = { Text("ヘルプの説明文") },
                    )
                }
            }
        }

        rule.onNode(hasContentDescription("閾値の説明を表示")).performClick()

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
    }
}

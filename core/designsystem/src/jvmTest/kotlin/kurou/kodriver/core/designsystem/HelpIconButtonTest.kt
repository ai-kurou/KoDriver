package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `状態復元後もヘルプ内容の表示状態が維持される`() =
        runComposeUiTest {
            var registry by mutableStateOf(SaveableStateRegistry(null) { true })
            var visible by mutableStateOf(true)
            setContent {
                if (visible) {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                        KoDriverTheme {
                            HelpIconButton(
                                contentDescription = "閾値の説明を表示",
                                sheetContent = { Text("ヘルプの説明文") },
                            )
                        }
                    }
                }
            }
            onNode(hasContentDescription("閾値の説明を表示")).performClick()
            onNodeWithText("ヘルプの説明文").assertIsDisplayed()

            val saved = registry.performSave()
            visible = false
            waitForIdle()
            registry = SaveableStateRegistry(saved) { true }
            visible = true
            waitForIdle()

            waitUntilExactlyOneExists(hasText("ヘルプの説明文"))
            onNodeWithText("ヘルプの説明文").assertIsDisplayed()
        }
}

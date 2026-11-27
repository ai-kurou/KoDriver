package kurou.kodriver.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class KoDriverHotkeyCardTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic = mockk<HapticFeedback>()
    private val onSelectModifier = mockk<(String) -> Unit>()
    private val onAssign = mockk<(Int) -> Unit>()
    private val onClear = mockk<() -> Unit>()
    private val onRetry = mockk<() -> Unit>()
    private val onOpenGuide = mockk<() -> Unit>()

    private var clearEnabled by mutableStateOf(true)
    private var errorText by mutableStateOf<String?>(null)
    private var registration by mutableStateOf<KoDriverHotkeyRegistration?>(null)
    private var showGuideLink by mutableStateOf(true)

    private fun setContent() {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverHotkeyCard(
                        texts =
                            KoDriverHotkeyCardTexts(
                                title = "タイトル",
                                clear = "解除",
                                retry = "再試行",
                                hint = "説明",
                                guideLink = "案内",
                            ),
                        modifierOptions = listOf("FIRST", "SECOND"),
                        selectedModifier = "FIRST",
                        modifierLabel = { "修飾 $it" },
                        onSelectModifier = { onSelectModifier(it) },
                        keyText = "割り当てなし",
                        onAssign = { onAssign(it) },
                        clearEnabled = clearEnabled,
                        onClear = { onClear() },
                        errorText = errorText,
                        registration = registration,
                        onRetry = { onRetry() },
                        onOpenGuide = { onOpenGuide() },
                        testTagPrefix = "hotkey",
                        showGuideLink = showGuideLink,
                    )
                }
            }
        }
    }

    @Test
    fun `渡した文言を表示しエラーと登録状態はnullなら表示しない`() {
        setContent()

        listOf("タイトル", "解除", "説明", "案内", "割り当てなし", "修飾 FIRST").forEach {
            rule.onNodeWithText(it).assertExists()
        }
        rule.onNodeWithTag("hotkey_card").assertExists()
        rule.onNodeWithText("再試行").assertDoesNotExist()
        confirmVerified(haptic, onSelectModifier, onAssign, onClear, onRetry, onOpenGuide)
    }

    @Test
    fun `エラーと登録状態と案内リンクの表示を引数の変化に合わせて切り替える`() {
        clearEnabled = false
        setContent()
        rule.onNodeWithText("解除").assertIsNotEnabled()

        clearEnabled = true
        errorText = "重複しています"
        registration = KoDriverHotkeyRegistration(text = "登録に失敗", canRetry = true)
        rule.waitForIdle()

        rule.onNodeWithText("解除").assertIsEnabled()
        rule.onNodeWithText("重複しています").assertExists()
        rule.onNodeWithText("登録に失敗").assertExists()
        rule.onNodeWithText("再試行").assertExists()

        registration = KoDriverHotkeyRegistration(text = "登録済み")
        rule.waitForIdle()

        rule.onNodeWithText("登録済み").assertExists()
        rule.onNodeWithText("再試行").assertDoesNotExist()

        errorText = null
        registration = null
        showGuideLink = false
        rule.waitForIdle()

        rule.onNodeWithText("重複しています").assertDoesNotExist()
        rule.onNodeWithText("登録済み").assertDoesNotExist()
        rule.onNodeWithText("案内").assertDoesNotExist()
        confirmVerified(haptic, onSelectModifier, onAssign, onClear, onRetry, onOpenGuide)
    }

    @Test
    fun `解除と再試行と案内リンクは触覚フィードバックを発生させて通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onClear() } returns Unit
        every { onRetry() } returns Unit
        every { onOpenGuide() } returns Unit
        registration = KoDriverHotkeyRegistration(text = "登録に失敗", canRetry = true)
        setContent()

        rule.onNodeWithText("解除").performClick()
        rule.onNodeWithText("再試行").performClick()
        rule.onNodeWithText("案内").performClick()

        verify(exactly = 3) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onClear() }
        verify(exactly = 1) { onRetry() }
        verify(exactly = 1) { onOpenGuide() }
        confirmVerified(haptic, onSelectModifier, onAssign, onClear, onRetry, onOpenGuide)
    }

    @Test
    fun `修飾キーを選択すると選んだ値を通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onSelectModifier("SECOND") } returns Unit
        setContent()

        rule.onNodeWithTag("hotkey_modifiers").performClick()
        rule.onNodeWithTag("hotkey_modifiers_SECOND").performClick()

        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onSelectModifier("SECOND") }
        confirmVerified(haptic, onSelectModifier, onAssign, onClear, onRetry, onOpenGuide)
    }

    @Test
    fun `入力欄で対象キーを押すと仮想キーコードを通知し対象外のキーは無視する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onAssign(0x41) } returns Unit
        setContent()

        rule.onNodeWithTag("hotkey_key").performClick().performKeyInput {
            pressKey(Key.A)
            pressKey(Key.F1)
        }

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onAssign(0x41) }
        confirmVerified(haptic, onSelectModifier, onAssign, onClear, onRetry, onOpenGuide)
    }
}

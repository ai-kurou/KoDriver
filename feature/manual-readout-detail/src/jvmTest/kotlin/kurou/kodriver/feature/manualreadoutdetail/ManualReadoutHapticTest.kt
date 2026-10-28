package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutHapticTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

    @Composable
    private fun HapticContent(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalHapticFeedback provides haptic) {
            KoDriverTheme { content() }
        }
    }

    @Test
    fun `追加ボタンと削除と試聴開始停止で通知し状態更新では通知しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val key = lmuWindowsManualReadoutItemKeys().first()
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    availableItemKeys = listOf(key),
                    selectedItemKeys = listOf(key),
                    isTextToSpeechAvailable = true,
                ),
            )
        val removed = mutableListOf<String>()
        var previews = 0
        rule.setContent {
            HapticContent {
                ManualReadoutItemsCard(state.value, {}, { removed.add(it) }, { _, _ -> }, { _, _ -> }, { previews++ })
            }
        }
        rule.onNodeWithText("＋ 項目を追加").assertIsNotEnabled()
        rule.onNodeWithText("試聴").performClick()
        rule.runOnIdle { state.value = state.value.copy(isPreviewing = true) }
        rule.onNodeWithText("試聴を停止").performClick()
        rule.onNodeWithContentDescription("バーチャルエネルギー残量を削除").performClick()
        rule.runOnIdle { state.value = state.value.copy(selectedItemKeys = emptyList()) }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        assertEquals(listOf(key), removed)
        assertEquals(2, previews)
        verify(exactly = 4) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `ピッカー項目を選ぶと一度通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val key = lmuWindowsManualReadoutItemKeys().first()
        val selected = mutableListOf<String>()
        rule.setContent {
            HapticContent { ManualReadoutItemPickerSheetContent(listOf(key), 0) { selected.add(it) } }
        }
        rule
            .onNodeWithText("バーチャルエネルギー残量")
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        assertEquals(listOf(key), selected)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `名前編集開始とチェックとDoneは通知し入力では通知しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val saved = mutableListOf<String>()
        rule.setContent { HapticContent { ManualReadoutSetNameRow("", 1) { saved.add(it) } } }
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("名前")
        rule.onNodeWithContentDescription("確定").performClick()
        rule.onNodeWithTag("manual_readout_set_name").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performImeAction()
        assertEquals(listOf("名前", ""), saved)
        verify(exactly = 4) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `名前のフォーカス喪失による保存では通知を追加しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val saved = mutableListOf<String>()
        rule.setContent {
            HapticContent {
                Column {
                    ManualReadoutSetNameRow("", 1) { saved.add(it) }
                    OutlinedTextField(value = "別の入力欄", onValueChange = {})
                }
            }
        }
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("名前")
        rule.onNodeWithText("別の入力欄").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").assertDoesNotExist()
        assertEquals(listOf("名前"), saved)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `ホットキーの有効入力と解除と再試行と修飾キー操作で通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        val state = mutableStateOf(ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id))
        val assigned = mutableListOf<Int>()
        val modifiers = mutableListOf<ManualReadoutHotkeyModifiers>()
        var cleared = 0
        var retries = 0
        rule.setContent {
            HapticContent {
                ManualReadoutHotkeyCard(
                    state.value,
                    { assigned.add(it) },
                    { cleared++ },
                    { retries++ },
                    { modifiers.add(it) },
                    {},
                    false,
                )
            }
        }
        rule.onNodeWithText("割り当てを解除").assertIsNotEnabled()
        rule.onNodeWithTag("manual_readout_hotkey_key").performClick().performKeyInput {
            pressKey(Key.A)
            pressKey(Key.F1)
        }
        rule.runOnIdle {
            state.value =
                state.value.copy(
                    hotkeyVirtualKeyCode = 0x41,
                    hotkeyRegistrationState = HotkeyRegistrationState.Conflict,
                )
        }
        rule.onNodeWithText("割り当てを解除").performClick()
        rule.onNodeWithText("再試行").performClick()
        rule.onNodeWithTag("manual_readout_hotkey_modifiers").performClick()
        rule.onNodeWithTag("manual_readout_hotkey_modifiers_CTRL_SHIFT").performClick()
        assertEquals(listOf(0x41), assigned)
        assertEquals(listOf(ManualReadoutHotkeyModifiers.CTRL_SHIFT), modifiers)
        assertEquals(1, cleared)
        assertEquals(1, retries)
        verify(exactly = 5) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}

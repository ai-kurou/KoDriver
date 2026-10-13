package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherReadoutStartSoundDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherReadoutStartSoundDetailUiState = OtherReadoutStartSoundDetailUiState(),
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherReadoutStartSoundDetailDialogContent(
                uiState = uiState,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
            )
        }
    }

    private val haptic: HapticFeedback = mockk()

    @Test
    fun `OKボタンをクリックするとonConfirmが呼ばれる`() {
        var confirmCount = 0
        setContent(onConfirm = { confirmCount++ })

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, confirmCount)
    }

    @Test
    fun `キャンセルボタンをクリックするとonDismissが呼ばれる`() {
        var dismissCount = 0
        setContent(onDismiss = { dismissCount++ })

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun `すべての種別ラベルが表示されている`() {
        setContent()

        rule.onNodeWithText("電子ノイズ").fetchSemanticsNode()
        rule.onNodeWithText("Formula無線").fetchSemanticsNode()
    }

    @Test
    fun `選択行がラジオボタンのroleと選択状態を公開しクリックで一度だけ選択と触感を更新する`() {
        var selected by mutableStateOf(ReadoutStartSoundType.FORMULA_RADIO)
        val selections = mutableListOf<ReadoutStartSoundType>()
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    OtherReadoutStartSoundDetailDialogContent(
                        uiState = OtherReadoutStartSoundDetailUiState(pendingType = selected),
                        onTypeSelected = {
                            selected = it
                            selections.add(it)
                        },
                    )
                }
            }
        }

        val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule
            .onNodeWithText("Formula無線")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsSelected()
        rule
            .onNodeWithText("電子ノイズ")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("Formula無線").assertIsNotSelected()
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        rule.onNodeWithText("電子ノイズ").assertIsSelected().performClick()

        assertEquals(listOf(ReadoutStartSoundType.ELECTRONIC_NOISE, ReadoutStartSoundType.ELECTRONIC_NOISE), selections)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}

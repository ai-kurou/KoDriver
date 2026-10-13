package kurou.kodriver.feature.otheroverlaytextsizedetail

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
import kurou.kodriver.domain.model.OverlayTextSize
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherOverlayTextSizeDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherOverlayTextSizeDetailUiState = OtherOverlayTextSizeDetailUiState(),
        onOverlayTextSizeSelected: (OverlayTextSize) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherOverlayTextSizeDetailDialogContent(
                uiState = uiState,
                onOverlayTextSizeSelected = onOverlayTextSizeSelected,
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
    fun `すべての文字サイズラベルが表示されている`() {
        setContent()

        listOf("極小", "小", "中", "大", "特大", "超特大", "最大").forEach { label ->
            rule.onNodeWithText(label).fetchSemanticsNode()
        }
    }

    @Test
    fun `文字サイズをクリックするとonOverlayTextSizeSelectedが呼ばれる`() {
        var selectedOverlayTextSize: OverlayTextSize? = null
        setContent(onOverlayTextSizeSelected = { selectedOverlayTextSize = it })

        val sizesByLabel =
            listOf(
                "極小" to OverlayTextSize.EXTRA_SMALL,
                "小" to OverlayTextSize.SMALL,
                "中" to OverlayTextSize.MEDIUM,
                "大" to OverlayTextSize.LARGE,
                "特大" to OverlayTextSize.EXTRA_LARGE,
                "超特大" to OverlayTextSize.HUGE,
                "最大" to OverlayTextSize.MAXIMUM,
            )
        sizesByLabel.forEach { (label, overlayTextSize) ->
            rule.onNodeWithText(label).performClick()

            assertEquals(overlayTextSize, selectedOverlayTextSize)
        }
    }

    @Test
    fun `選択行がラジオボタンのroleと選択状態を公開しクリックで一度だけ選択と触感を更新する`() {
        var selected by mutableStateOf(OverlayTextSize.MEDIUM)
        val selections = mutableListOf<OverlayTextSize>()
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    OtherOverlayTextSizeDetailDialogContent(
                        uiState = OtherOverlayTextSizeDetailUiState(pendingOverlayTextSize = selected),
                        onOverlayTextSizeSelected = {
                            selected = it
                            selections.add(it)
                        },
                    )
                }
            }
        }

        val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule
            .onNodeWithText("中")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsSelected()
        rule
            .onNodeWithText("大")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("中").assertIsNotSelected()
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        rule.onNodeWithText("大").assertIsSelected().performClick()

        assertEquals(listOf(OverlayTextSize.LARGE, OverlayTextSize.LARGE), selections)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}

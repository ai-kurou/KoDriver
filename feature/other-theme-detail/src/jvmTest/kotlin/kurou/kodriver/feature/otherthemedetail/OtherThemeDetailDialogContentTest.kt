package kurou.kodriver.feature.otherthemedetail

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
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherThemeDetailDialogContentTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setContent(
        uiState: OtherThemeDetailUiState = OtherThemeDetailUiState(),
        onThemeModeSelected: (ThemeMode) -> Unit = {},
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            OtherThemeDetailDialogContent(
                uiState = uiState,
                onThemeModeSelected = onThemeModeSelected,
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
    fun `すべてのテーマモードラベルが表示されている`() {
        setContent()

        rule.onNodeWithText("システムに従う").fetchSemanticsNode()
        rule.onNodeWithText("ライト").fetchSemanticsNode()
        rule.onNodeWithText("ダーク").fetchSemanticsNode()
    }

    @Test
    fun `テーマモードをクリックするとonThemeModeSelectedが呼ばれる`() {
        var selectedThemeMode: ThemeMode? = null
        setContent(onThemeModeSelected = { selectedThemeMode = it })

        rule.onNodeWithText("ダーク").performClick()

        assertEquals(ThemeMode.DARK, selectedThemeMode)
    }

    @Test
    fun `選択行がラジオボタンのroleと選択状態を公開しクリックで一度だけ選択と触感を更新する`() {
        var selected by mutableStateOf(ThemeMode.SYSTEM)
        val selections = mutableListOf<ThemeMode>()
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    OtherThemeDetailDialogContent(
                        uiState = OtherThemeDetailUiState(pendingThemeMode = selected),
                        onThemeModeSelected = {
                            selected = it
                            selections.add(it)
                        },
                    )
                }
            }
        }

        val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule
            .onNodeWithText("システムに従う")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsSelected()
        rule
            .onNodeWithText("ダーク")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("システムに従う").assertIsNotSelected()
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        rule.onNodeWithText("ダーク").assertIsSelected().performClick()

        assertEquals(listOf(ThemeMode.DARK, ThemeMode.DARK), selections)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}

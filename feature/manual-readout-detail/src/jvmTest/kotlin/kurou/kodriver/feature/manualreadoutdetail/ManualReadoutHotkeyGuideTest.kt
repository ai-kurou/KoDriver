package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class ManualReadoutHotkeyGuideTest {
    @get:Rule
    val rule = createComposeRule()

    private val uriHandler: UriHandler = mockk()
    private val haptic: HapticFeedback = mockk()
    private val onOpenGuide: () -> Unit = mockk()

    @Test
    fun `ホットキーカードのガイドリンクを表示してクリックするとSimHub手順書を開く`() {
        every {
            uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md")
        } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                KoDriverTheme {
                    ManualReadoutSetDetailPane(
                        uiState =
                            ManualReadoutSetDetailUiState(
                                simulatorId = Simulator.LmuWindows.id,
                                setNumber = 1,
                            ),
                        onAddItem = {},
                        onDelete = {},
                        showHotkey = true,
                    )
                }
            }
        }
        rule
            .onNodeWithText("ステアリングのボタンから実行する設定を開く")
            .performScrollTo()
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }

        verify(exactly = 1) {
            uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md")
        }
        confirmVerified(uriHandler)
    }

    @Test
    fun `ガイドリンクはホットキーカードを非表示にすると表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                        ),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = false,
                )
            }
        }
        rule
            .onNodeWithText("ステアリングのボタンから実行する設定を開く")
            .assertDoesNotExist()
    }

    @Test
    fun `ガイドリンクをクリックすると触覚フィードバックとコールバックを一度実行する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onOpenGuide() } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    ManualReadoutHotkeyCard(
                        state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id),
                        onAssign = {},
                        onClear = {},
                        onRetry = {},
                        onSelectModifiers = {},
                        onOpenGuide = onOpenGuide,
                    )
                }
            }
        }
        rule
            .onNodeWithText("ステアリングのボタンから実行する設定を開く")
            .performSemanticsAction(SemanticsActions.OnClick) { it() }

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onOpenGuide() }
        confirmVerified(haptic, onOpenGuide)
    }
}

package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneUriWiringTest {
    private val uriHandler: UriHandler = mockk()
    private val haptic: HapticFeedback = mockk()

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Windowsの説明シートのリンクをクリックするとSimHub手順書のURLが1回開かれる`() {
        mockkStatic(::isManualReadoutHotkeySupported)
        try {
            every { isManualReadoutHotkeySupported() } returns true
            every {
                uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md")
            } returns Unit
            rule.setContent {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    KoDriverTheme {
                        ManualReadoutListPaneContent(
                            uiState = ManualReadoutListUiState(),
                            simulatorId = "lmu_windows",
                            simulatorName = "LMU",
                            onAddSet = {},
                            onReadSet = {},
                        )
                    }
                }
            }
            rule.onNodeWithContentDescription("手動読み上げの説明を表示").performClick()
            rule
                .onNodeWithText("ステアリングのボタンから実行する方法を見る")
                .performScrollTo()
                .performSemanticsAction(SemanticsActions.OnClick)

            verify(exactly = 1) {
                uriHandler.openUri("https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md")
            }
            confirmVerified(uriHandler)
        } finally {
            unmockkStatic(::isManualReadoutHotkeySupported)
        }
    }

    @Test
    fun `説明シートのリンクをクリックすると触覚フィードバックとコールバックが1回ずつ呼ばれる`() {
        var openCount = 0
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    ManualReadoutHintSheetContent(
                        showHotkeyHint = true,
                        onOpenHotkeyGuide = { openCount++ },
                    )
                }
            }
        }
        rule
            .onNodeWithText("ステアリングのボタンから実行する方法を見る")
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(1, openCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `GT7の説明シートにはSimHub手順書のリンクを表示しない`() {
        mockkStatic(::isManualReadoutHotkeySupported)
        try {
            every { isManualReadoutHotkeySupported() } returns true
            rule.setContent {
                KoDriverTheme {
                    ManualReadoutListPaneContent(
                        uiState = ManualReadoutListUiState(),
                        simulatorId = "gt7_ps5",
                        simulatorName = "GT7",
                        onAddSet = {},
                        onReadSet = {},
                    )
                }
            }
            rule.onNodeWithContentDescription("手動読み上げの説明を表示").performClick()

            rule.onNodeWithText("ホットキー").assertExists()
            rule.onNodeWithText("ステアリングのボタンから実行する方法を見る").assertDoesNotExist()
        } finally {
            unmockkStatic(::isManualReadoutHotkeySupported)
        }
    }

    @Test
    fun `リンク非表示指定の説明シートにはホットキー説明だけを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutHintSheetContent(showHotkeyHint = true, showSimHubGuideLink = false)
            }
        }
        rule.onNodeWithText("ステアリングのボタンから実行する方法を見る").assertDoesNotExist()
    }
}

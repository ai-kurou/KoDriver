package kurou.kodriver.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class TtsUnavailableDialogTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()
    private val installEngine: () -> Unit = mockk()
    private val openLanguageSettings: () -> Unit = mockk()
    private val openWindowsSettings: () -> Unit = mockk()

    @Test
    fun `エンジン不足の文言とインストール操作を表示し操作後も残る`() {
        every { installEngine() } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        show(TtsUnavailableGuidance.EngineMissing)
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed()
        rule
            .onNodeWithText(
                "この端末に音声合成アプリがありません。インストールすると、KoDriverが音声で案内できるようになります。",
            ).assertIsDisplayed()
        rule.onNodeWithText("インストール").performClick()
        rule.onNodeWithText("このまま使う").assertIsDisplayed()
        verify(exactly = 1) { installEngine() }
        verify(exactly = 0) { openLanguageSettings() }
        verify(exactly = 0) { openWindowsSettings() }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings, haptic)
    }

    @Test
    fun `言語データ不足の文言と設定操作を表示し操作後も残る`() {
        every { openLanguageSettings() } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        show(TtsUnavailableGuidance.LanguageDataMissing)
        rule.onNodeWithText("日本語の音声データがありません").assertIsDisplayed()
        rule
            .onNodeWithText(
                "音声合成アプリに日本語の音声データがありません。設定からダウンロードすると、KoDriverが音声で案内できるようになります。",
            ).assertIsDisplayed()
        rule.onNodeWithText("設定を開く").performClick()
        rule.onNodeWithText("このまま使う").assertIsDisplayed()
        verify(exactly = 0) { installEngine() }
        verify(exactly = 1) { openLanguageSettings() }
        verify(exactly = 0) { openWindowsSettings() }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings, haptic)
    }

    @Test
    fun `Windows音声不足の文言と再起動案内と設定操作を表示し操作後も残る`() {
        every { openWindowsSettings() } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        show(TtsUnavailableGuidance.WindowsSpeechUnavailable)
        rule.onNodeWithText("Windowsで日本語音声を利用できません").assertIsDisplayed()
        rule
            .onNodeWithText(
                "Windowsの日本語音声が見つかりません。音声ページを開き、「音声の追加」から日本語の音声を追加してください。",
            ).assertIsDisplayed()
        rule.onNodeWithText("追加した後は、KoDriverを再起動してください。").assertIsDisplayed()
        rule.onNodeWithText("Windowsの設定を開く").performClick()
        rule.onNodeWithText("このまま使う").assertIsDisplayed()
        verify(exactly = 0) { installEngine() }
        verify(exactly = 0) { openLanguageSettings() }
        verify(exactly = 1) { openWindowsSettings() }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings, haptic)
    }

    @Test
    fun `このまま使うを押すとハプティックフィードバックと閉じる操作を実行する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var dismissCount = 0
        show(TtsUnavailableGuidance.EngineMissing, onDismiss = { dismissCount++ })

        rule.onNodeWithText("このまま使う").performClick()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 0) { installEngine() }
        verify(exactly = 0) { openLanguageSettings() }
        verify(exactly = 0) { openWindowsSettings() }
        confirmVerified(haptic, installEngine, openLanguageSettings, openWindowsSettings)
        assertEquals(1, dismissCount)
    }

    private fun show(
        reason: TtsUnavailableGuidance,
        onDismiss: () -> Unit = {},
    ) {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppTheme {
                    TtsUnavailableDialog(
                        reason = reason,
                        onPrimaryClick =
                            when (reason) {
                                TtsUnavailableGuidance.EngineMissing -> installEngine
                                TtsUnavailableGuidance.LanguageDataMissing -> openLanguageSettings
                                TtsUnavailableGuidance.WindowsSpeechUnavailable -> openWindowsSettings
                            },
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }
}

package kurou.kodriver.presentation

import androidx.compose.runtime.mutableStateOf
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

class TtsUnavailableDialogTest {
    @get:Rule
    val rule = createComposeRule()

    private val installEngine: () -> Unit = mockk()
    private val openLanguageSettings: () -> Unit = mockk()
    private val openWindowsSettings: () -> Unit = mockk()

    @Test
    fun `エンジン不足の文言とインストール操作を表示し操作後も残る`() {
        every { installEngine() } returns Unit
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
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings)
    }

    @Test
    fun `言語データ不足の文言と設定操作を表示し操作後も残る`() {
        every { openLanguageSettings() } returns Unit
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
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings)
    }

    @Test
    fun `Windows音声不足の文言と再起動案内と設定操作を表示し操作後も残る`() {
        every { openWindowsSettings() } returns Unit
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
        confirmVerified(installEngine, openLanguageSettings, openWindowsSettings)
    }

    @Test
    fun `利用可能なら非表示で利用不可になれば表示し解消すると消える`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(null)
        rule.setContent {
            AppTheme {
                TtsUnavailableDialogHost(reason.value, installEngine, openLanguageSettings, openWindowsSettings)
            }
        }
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.EngineMissing }
        rule.onNodeWithText("インストール").assertIsDisplayed()
        rule.runOnIdle { reason.value = null }
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
    }

    @Test
    fun `このまま使うで閉じた後は理由が変わっても再表示しない`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(TtsUnavailableGuidance.EngineMissing)
        rule.setContent {
            AppTheme {
                TtsUnavailableDialogHost(reason.value, installEngine, openLanguageSettings, openWindowsSettings)
            }
        }
        rule.onNodeWithText("このまま使う").performClick()
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
        rule.runOnIdle { reason.value = null }
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.LanguageDataMissing }
        rule.onNodeWithText("設定を開く").assertDoesNotExist()
    }

    private fun show(reason: TtsUnavailableGuidance) {
        rule.setContent {
            AppTheme {
                TtsUnavailableDialogHost(reason, installEngine, openLanguageSettings, openWindowsSettings)
            }
        }
    }
}

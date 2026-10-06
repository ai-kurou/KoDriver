package kurou.kodriver.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
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

class TtsUnavailableBannerTest {
    @get:Rule
    val rule = createComposeRule()

    private val action: () -> Unit = mockk()

    @Test
    fun `起動時はバナーだけ表示し案内はタップで開いて閉じた後も再表示できる`() {
        show(TtsUnavailableGuidance.EngineMissing)
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed().performClick()
        rule.onNodeWithText("インストール").assertIsDisplayed()
        rule.onNodeWithText("このまま使う").performClick()
        rule.onNodeWithText("インストール").assertDoesNotExist()
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed().performClick()
        rule.onNodeWithText("インストール").assertIsDisplayed()
    }

    @Test
    fun `日本語データ不足のバナーから設定操作を実行しても案内を残す`() {
        every { action() } returns Unit
        show(TtsUnavailableGuidance.LanguageDataMissing)
        rule.onNodeWithText("日本語の音声データがありません").performClick()
        rule.onNodeWithText("設定を開く").performClick()
        rule.onNodeWithText("このまま使う").assertIsDisplayed()
        verify(exactly = 1) { action() }
        confirmVerified(action)
    }

    @Test
    fun `Windows音声不足のバナーから設定と再起動の案内を表示する`() {
        show(TtsUnavailableGuidance.WindowsSpeechUnavailable)
        rule.onNodeWithText("Windowsで日本語音声を利用できません").performClick()
        rule.onNodeWithText("Windowsの設定を開く").assertIsDisplayed()
        rule.onNodeWithText("追加した後は、KoDriverを再起動してください。").assertIsDisplayed()
    }

    @Test
    fun `利用可能なら非表示で利用不可になるとバナーだけ表示し解消すると案内も消える`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(null)
        rule.setContent { AppTheme { TtsUnavailableBannerHost(reason.value, action) } }
        rule.onNodeWithText("音声読み上げを利用できません").assertDoesNotExist()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.EngineMissing }
        rule.onNodeWithText("インストール").assertDoesNotExist()
        rule.onNodeWithText("音声読み上げを利用できません").performClick()
        rule.runOnIdle { reason.value = null }
        rule.onNodeWithText("音声読み上げを利用できません").assertDoesNotExist()
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.EngineMissing }
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed()
        rule.onNodeWithText("インストール").assertDoesNotExist()
    }

    @Test
    fun `利用不可理由が変わると案内を閉じ新しいバナーから開ける`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(TtsUnavailableGuidance.EngineMissing)
        rule.setContent { AppTheme { TtsUnavailableBannerHost(reason.value, action) } }
        rule.onNodeWithText("音声読み上げを利用できません").performClick()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.LanguageDataMissing }
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
        rule.onNodeWithText("日本語の音声データがありません").performClick()
        rule.onNodeWithText("設定を開く").assertIsDisplayed()
    }

    @Test
    fun `バナーをタップするとハプティックフィードバックが発生する`() {
        val feedback = mutableListOf<HapticFeedbackType>()
        val haptic =
            object : HapticFeedback {
                override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                    feedback.add(hapticFeedbackType)
                }
            }
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppTheme { TtsUnavailableBannerHost(TtsUnavailableGuidance.EngineMissing, action) }
            }
        }
        rule.onNodeWithText("音声読み上げを利用できません").performClick()
        assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
    }

    private fun show(reason: TtsUnavailableGuidance) {
        rule.setContent { AppTheme { TtsUnavailableBannerHost(reason, action) } }
        rule.onNodeWithText("このまま使う").assertDoesNotExist()
    }
}

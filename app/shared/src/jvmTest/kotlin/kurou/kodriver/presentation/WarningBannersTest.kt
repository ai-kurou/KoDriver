package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Rule
import org.junit.Test

class WarningBannersTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `TTS警告は登場し退場中も直近の理由と隙間を保持して完了後に消える`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(null)
        rule.setContent {
            AppTheme {
                Column {
                    WarningBanners(true, reason.value, {}, LowVolumeWarningUiState(), {}, {})
                }
            }
        }
        rule.onNodeWithTag("connectionTtsBannerGap").assertDoesNotExist()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.EngineMissing }
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed()
        rule.runOnIdle { reason.value = TtsUnavailableGuidance.LanguageDataMissing }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("日本語の音声データがありません").assertIsDisplayed()
        rule.runOnIdle { reason.value = null }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("日本語の音声データがありません").assertExists()
        rule.onNodeWithTag("connectionTtsBannerGap").assertExists()
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithText("日本語の音声データがありません").assertDoesNotExist()
        rule.onNodeWithTag("connectionTtsBannerGap").assertDoesNotExist()
    }

    @Test
    fun `音量警告は登場し解消後も退場中は直近の警告と隙間を保持する`() {
        val warning = mutableStateOf(LowVolumeWarningUiState())
        rule.setContent {
            AppTheme {
                Column {
                    WarningBanners(true, null, {}, warning.value, {}, {})
                }
            }
        }
        rule.onNodeWithTag("connectionTtsBannerGap").assertDoesNotExist()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle { warning.value = LowVolumeWarningUiState(deviceVolume = 20) }
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("端末の音量が低くなっています").assertIsDisplayed()
        rule.runOnIdle { warning.value = LowVolumeWarningUiState(soundVolume = 20) }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertIsDisplayed()
        rule.runOnIdle { warning.value = LowVolumeWarningUiState() }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertExists()
        rule.onNodeWithTag("connectionTtsBannerGap").assertExists()
        rule.mainClock.advanceTimeBy(200)
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertDoesNotExist()
        rule.onNodeWithTag("connectionTtsBannerGap").assertDoesNotExist()
    }

    @Test
    fun `TTS警告の間の音量変更は退場中の音量警告に反映せずTTS解消後に反映する`() {
        val reason = mutableStateOf<TtsUnavailableGuidance?>(null)
        val warning = mutableStateOf(LowVolumeWarningUiState(deviceVolume = 20))
        rule.setContent {
            AppTheme {
                Column {
                    WarningBanners(false, reason.value, {}, warning.value, {}, {})
                }
            }
        }
        rule.onNodeWithText("端末の音量が低くなっています").assertIsDisplayed()
        rule.mainClock.autoAdvance = false
        rule.runOnIdle {
            reason.value = TtsUnavailableGuidance.EngineMissing
            warning.value = LowVolumeWarningUiState(soundVolume = 20)
        }
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("端末の音量が低くなっています").assertExists()
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertDoesNotExist()
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithText("端末の音量が低くなっています").assertDoesNotExist()
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed()
        rule.onNodeWithTag("connectionTtsBannerGap").assertDoesNotExist()
        rule.runOnIdle { reason.value = null }
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("音声読み上げを利用できません").assertDoesNotExist()
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertIsDisplayed()
    }
}

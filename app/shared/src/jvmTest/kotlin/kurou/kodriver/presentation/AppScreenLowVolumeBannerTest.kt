package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenLowVolumeBannerTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `TTS警告がないとき音量警告を表示しTTS警告があるときは隠す`() {
        val guidance = mutableStateOf<TtsUnavailableGuidance?>(null)
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                lowVolumeWarning = LowVolumeWarningUiState(deviceVolume = 20),
                ttsUnavailableGuidance = guidance.value,
            )
        }
        rule.onNodeWithText("端末の音量が低くなっています").assertIsDisplayed()
        rule.runOnIdle { guidance.value = TtsUnavailableGuidance.EngineMissing }
        rule.onNodeWithText("端末の音量が低くなっています").assertDoesNotExist()
        rule.onNodeWithText("音声読み上げを利用できません").assertIsDisplayed()
    }

    @Test
    fun `音量バナー本文タップでその他へ移動しVolume選択コールバックを呼ぶ`() {
        var selectionCount = 0
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                lowVolumeWarning = LowVolumeWarningUiState(soundVolume = 20),
                onLowVolumeBannerClick = { selectionCount++ },
                otherContent = { _, _ -> Text("Other detail") },
            )
        }

        rule.onNodeWithText("KoDriverの音量が低くなっています").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Other detail").assertIsDisplayed()
        assertEquals(1, selectionCount)
    }

    @Test
    fun `音量バナーの閉じる操作でdismissコールバックを呼ぶ`() {
        var dismissCount = 0
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                lowVolumeWarning = LowVolumeWarningUiState(deviceVolume = 20),
                onLowVolumeBannerDismiss = { dismissCount++ },
            )
        }

        rule.onNodeWithContentDescription("音量警告を閉じる").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun `接続バナーと音量バナー表示時に1dp gapを表示する`() {
        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                bannerUiState = ConnectionBannerUiState(isVisible = true),
                lowVolumeWarning = LowVolumeWarningUiState(deviceVolume = 20),
            )
        }

        rule.onNodeWithTag("connectionTtsBannerGap").assertIsDisplayed()
        rule.onNodeWithText("端末の音量が低くなっています").assertIsDisplayed()
    }
}

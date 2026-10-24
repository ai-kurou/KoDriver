@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListViewModelTtsGuidanceTest : OtherListViewModelTestFixture() {
    @Test
    fun `初期状態ではTTS案内項目は表示されない`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            val items = viewModel.uiState.first().items
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertFalse(items.contains(OtherListItemType.TtsLanguageDataMissing))
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }

    @Test
    fun `checkTextToSpeechAvailabilityでエンジン未インストールと判明した場合はエンジンインストール案内のみ表示される`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            coEvery {
                textToSpeechRepository.unavailableReason()
            } returns TextToSpeechUnavailableReason.EngineMissing
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.EngineMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertTrue(items.contains(OtherListItemType.TtsEngineMissing))
            assertEquals(items.sortedBy { it.ordinal }, items)
            assertFalse(items.contains(OtherListItemType.TtsLanguageDataMissing))
            coVerify(exactly = 1) { textToSpeechRepository.unavailableReason() }
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
                textToSpeechRepository,
            )
        }

    @Test
    fun `checkTextToSpeechAvailabilityで言語データ未インストールと判明した場合は言語データ設定案内のみ表示される`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            every { soundVolumeRepository.volume() } returns soundVolumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
            every { voiceRepository.voiceId() } returns voiceFlow
            every { themeRepository.observeThemeMode() } returns themeModeFlow
            every { serverIpRepository.serverIp() } returns serverIpFlow
            every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
            every { consolePortRepository.port() } returns consolePortFlow
            every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
            coEvery {
                textToSpeechRepository.unavailableReason()
            } returns TextToSpeechUnavailableReason.LanguageDataMissing
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            viewModel.checkTextToSpeechAvailability()

            val state = viewModel.uiState.first()
            assertEquals(TtsUnavailableGuidance.LanguageDataMissing, state.ttsUnavailableGuidance)
            val items = state.items
            assertFalse(items.contains(OtherListItemType.TtsEngineMissing))
            assertTrue(items.contains(OtherListItemType.TtsLanguageDataMissing))
            coVerify(exactly = 1) { textToSpeechRepository.unavailableReason() }
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                overlayVisibleRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
                textToSpeechRepository,
            )
        }
}

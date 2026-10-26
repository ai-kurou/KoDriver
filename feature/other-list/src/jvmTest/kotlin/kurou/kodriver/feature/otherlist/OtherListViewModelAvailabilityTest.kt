package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelAvailabilityTest : OtherListViewModelTestFixture() {
    @Test
    fun `checkStartupEnabledでOS起動時自動起動の状態を取得できる`() =
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
            coEvery { startupRegistrationRepository.isEnabled() } returns true
            val viewModel = createViewModel()

            viewModel.checkStartupEnabled()

            assertTrue(viewModel.uiState.first().startupEnabled)
            coVerify(exactly = 1) { startupRegistrationRepository.isEnabled() }
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
                startupRegistrationRepository,
            )
        }

    @Test
    fun `onStartupEnabledChangeでOS起動時自動起動の状態を保存できる`() =
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
            coEvery { startupRegistrationRepository.setEnabled(true) } returns Unit
            val viewModel = createViewModel()

            viewModel.onStartupEnabledChange(true)

            assertTrue(viewModel.uiState.first().startupEnabled)
            coVerify(exactly = 1) { startupRegistrationRepository.setEnabled(true) }
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
                startupRegistrationRepository,
            )
        }

    @Test
    fun `初期状態で権限が許可済みの場合accessLocalNetworkPermissionGrantedがtrueになる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = true)

            assertTrue(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 1) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

    @Test
    fun `初期状態で権限が未許可の場合accessLocalNetworkPermissionGrantedがfalseになる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = false)

            assertFalse(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 1) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

    @Test
    fun `checkAccessLocalNetworkPermissionで権限状態を再取得できる`() =
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
            val viewModel = createViewModel(accessLocalNetworkPermissionGranted = false)
            every { accessLocalNetworkPermissionRepository.isGranted() } returns true

            viewModel.checkAccessLocalNetworkPermission()

            assertTrue(viewModel.uiState.first().accessLocalNetworkPermissionGranted)
            verify(exactly = 2) { accessLocalNetworkPermissionRepository.isGranted() }
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
                accessLocalNetworkPermissionRepository,
            )
        }

}

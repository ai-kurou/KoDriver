package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.captureError
import java.io.IOException
import kotlin.test.Test

/**
 * 設定変更やチェックを `viewModelScope` で実行したときに、例外を記録して握りつぶすことを確認する。
 * 対象の呼び先（Flow / UseCase）には例外を捕まえる処理がないため、ViewModel 側で記録する。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelCaptureErrorsTest : OtherListViewModelTestFixture() {
    private fun stubObservedSettings() {
        every { soundVolumeRepository.volume() } returns soundVolumeFlow
        coEvery { deviceVolumeRepository.getVolume() } returns 60
        every { readoutStartSoundRepository.observeType() } returns readoutStartSoundFlow
        every { voiceRepository.voiceId() } returns voiceFlow
        every { serverIpRepository.serverIp() } returns serverIpFlow
        every { consoleAddressRepository.consoleAddress() } returns consoleAddressFlow
        every { consolePortRepository.port() } returns consolePortFlow
        every { overlayVisibleRepository.observeOverlayVisible() } returns overlayVisibleFlow
    }

    @Test
    fun `更新確認が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { appUpdateRepository.getLatestRelease() } throws error
                val viewModel = createViewModel(currentVersion = "1.0.0")

                viewModel.checkUpdate()
                advanceUntilIdle()

                coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, appUpdateRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `スタートアップ登録状態の確認が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { startupRegistrationRepository.isEnabled() } throws error
                val viewModel = createViewModel()

                viewModel.checkStartupEnabled()
                advanceUntilIdle()

                coVerify(exactly = 1) { startupRegistrationRepository.isEnabled() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, startupRegistrationRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `スタートアップ登録の変更が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { startupRegistrationRepository.setEnabled(true) } throws error
                val viewModel = createViewModel()

                viewModel.onStartupEnabledChange(true)
                advanceUntilIdle()

                coVerify(exactly = 1) { startupRegistrationRepository.setEnabled(true) }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, startupRegistrationRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `読み上げ利用可否の確認が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { textToSpeechRepository.unavailableReason() } throws error
                val viewModel = createViewModel()

                viewModel.checkTextToSpeechAvailability()
                advanceUntilIdle()

                coVerify(exactly = 1) { textToSpeechRepository.unavailableReason() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, textToSpeechRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `オーバーレイ表示の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { overlayVisibleRepository.saveOverlayVisible(false) } throws error
                val viewModel = createViewModel()

                viewModel.onOverlayVisibleChange(false)
                advanceUntilIdle()

                coVerify(exactly = 1) { overlayVisibleRepository.saveOverlayVisible(false) }
                verify(exactly = 1) { overlayVisibleRepository.observeOverlayVisible() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, overlayVisibleRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `画面スリープ無効の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { keepScreenOnRepository.saveKeepScreenOn(false) } throws error
                val viewModel = createViewModel()

                viewModel.onKeepScreenOnChange(false)
                advanceUntilIdle()

                coVerify(exactly = 1) { keepScreenOnRepository.saveKeepScreenOn(false) }
                verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, keepScreenOnRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `Dynamic Colorの保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { dynamicColorRepository.saveDynamicColorEnabled(true) } throws error
                val viewModel = createViewModel()

                viewModel.onDynamicColorEnabledChange(true)
                advanceUntilIdle()

                coVerify(exactly = 1) { dynamicColorRepository.saveDynamicColorEnabled(true) }
                verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, dynamicColorRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `ハプティックフィードバックの保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservedSettings()
                coEvery { hapticFeedbackEnabledRepository.saveHapticFeedbackEnabled(false) } throws error
                val viewModel = createViewModel()

                viewModel.onHapticFeedbackEnabledChange(false)
                advanceUntilIdle()

                coVerify(exactly = 1) { hapticFeedbackEnabledRepository.saveHapticFeedbackEnabled(false) }
                verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture, hapticFeedbackEnabledRepository)
            } finally {
                unmockkStatic(errorCapture)
            }
        }
}

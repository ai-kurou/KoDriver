package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLowVolumeWarningUseCaseTest {
    private val deviceRepository: DeviceVolumeRepository = mockk()
    private val soundRepository: SoundVolumePreferencesRepository = mockk()

    @Test
    fun `端末音量だけが低い場合に警告する`() = assertWarning(deviceVolume = 20, soundVolume = 80, expected = LowVolumeWarning(20, null))

    @Test
    fun `アプリ音量だけが低い場合に警告する`() =
        assertWarning(deviceVolume = 30, soundVolume = 79, expected = LowVolumeWarning(null, 79))

    @Test
    fun `両方低い場合に両方の値を警告する`() = assertWarning(deviceVolume = 29, soundVolume = 79, expected = LowVolumeWarning(29, 79))

    @Test
    fun `どちらも閾値以上なら警告しない`() = assertWarning(deviceVolume = 30, soundVolume = 80, expected = null)

    private fun assertWarning(
        deviceVolume: Int,
        soundVolume: Int,
        expected: LowVolumeWarning?,
    ) = runTest {
        every { deviceRepository.observeVolume() } returns MutableStateFlow(deviceVolume)
        every { soundRepository.volume() } returns MutableStateFlow(soundVolume)
        val useCase =
            ObserveLowVolumeWarningUseCase(
                ObserveDeviceVolumeUseCase(deviceRepository),
                ObserveSoundVolumeUseCase(soundRepository),
            )

        assertEquals(expected, useCase().first())
        verify(exactly = 1) { deviceRepository.observeVolume() }
        verify(exactly = 1) { soundRepository.volume() }
        confirmVerified(deviceRepository, soundRepository)
    }
}

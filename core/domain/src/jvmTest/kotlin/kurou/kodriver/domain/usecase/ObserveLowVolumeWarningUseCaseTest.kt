package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLowVolumeWarningUseCaseTest {
    private val deviceRepository: DeviceVolumeRepository = mockk()
    private val soundRepository: SoundVolumePreferencesRepository = mockk()
    private val enabledRepository: LowVolumeWarningEnabledPreferencesRepository = mockk()

    @Test
    fun `端末音量だけが低い場合に警告する`() = assertWarning(deviceVolume = 20, soundVolume = 80, expected = LowVolumeWarning(20, null))

    @Test
    fun `アプリ音量だけが低い場合に警告する`() =
        assertWarning(deviceVolume = 30, soundVolume = 79, expected = LowVolumeWarning(null, 79))

    @Test
    fun `両方低い場合に両方の値を警告する`() = assertWarning(deviceVolume = 29, soundVolume = 79, expected = LowVolumeWarning(29, 79))

    @Test
    fun `どちらも閾値以上なら警告しない`() = assertWarning(deviceVolume = 30, soundVolume = 80, expected = null)

    @Test
    fun `無効のときnull`() = assertWarning(deviceVolume = 20, soundVolume = 20, expected = null, enabled = false)

    @Test
    fun `無効から有効にすると警告が出る`() =
        runTest {
            val enabled = MutableStateFlow(false)
            every { deviceRepository.observeVolume() } returns MutableStateFlow(20)
            every { soundRepository.volume() } returns MutableStateFlow(80)
            every { enabledRepository.observeLowVolumeWarningEnabled() } returns enabled
            val useCase =
                ObserveLowVolumeWarningUseCase(
                    ObserveDeviceVolumeUseCase(deviceRepository),
                    ObserveSoundVolumeUseCase(soundRepository),
                    ObserveLowVolumeWarningEnabledUseCase(enabledRepository),
                )
            val result = mutableListOf<LowVolumeWarning?>()
            val job = launch { useCase().collect { result.add(it) } }
            runCurrent()
            enabled.value = true
            runCurrent()
            assertEquals(listOf(null, LowVolumeWarning(20, null)), result)
            job.cancel()
            verify(exactly = 1) { deviceRepository.observeVolume() }
            verify(exactly = 1) { soundRepository.volume() }
            verify(exactly = 1) { enabledRepository.observeLowVolumeWarningEnabled() }
            confirmVerified(deviceRepository, soundRepository, enabledRepository)
        }

    private fun assertWarning(
        deviceVolume: Int,
        soundVolume: Int,
        expected: LowVolumeWarning?,
        enabled: Boolean = true,
    ) = runTest {
        every { deviceRepository.observeVolume() } returns MutableStateFlow(deviceVolume)
        every { soundRepository.volume() } returns MutableStateFlow(soundVolume)
        every { enabledRepository.observeLowVolumeWarningEnabled() } returns MutableStateFlow(enabled)
        val useCase =
            ObserveLowVolumeWarningUseCase(
                ObserveDeviceVolumeUseCase(deviceRepository),
                ObserveSoundVolumeUseCase(soundRepository),
                ObserveLowVolumeWarningEnabledUseCase(enabledRepository),
            )

        assertEquals(expected, useCase().first())
        verify(exactly = 1) { deviceRepository.observeVolume() }
        verify(exactly = 1) { soundRepository.volume() }
        verify(exactly = 1) { enabledRepository.observeLowVolumeWarningEnabled() }
        confirmVerified(deviceRepository, soundRepository, enabledRepository)
    }
}

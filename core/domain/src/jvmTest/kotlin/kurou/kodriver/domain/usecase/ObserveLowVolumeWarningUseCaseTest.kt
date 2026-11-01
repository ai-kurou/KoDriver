package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveLowVolumeWarningUseCaseTest {
    private val deviceRepository: DeviceVolumeRepository = mockk()
    private val soundRepository: SoundVolumePreferencesRepository = mockk()
    private val enabledRepository: LowVolumeWarningEnabledPreferencesRepository = mockk()

    @Test
    fun `端末音量が閾値未満の場合に警告する`() =
        assertWarning(deviceVolume = 19, soundVolume = 70, expected = LowVolumeWarning(19, null))

    @Test
    fun `アプリ音量だけが低い場合に警告する`() =
        assertWarning(deviceVolume = 20, soundVolume = 69, expected = LowVolumeWarning(null, 69))

    @Test
    fun `閾値ちょうどなら警告しない`() = assertWarning(deviceVolume = 20, soundVolume = 70, expected = null)

    @Test
    fun `両方低い場合に両方の値を警告する`() = assertWarning(deviceVolume = 19, soundVolume = 69, expected = LowVolumeWarning(19, 69))

    @Test
    fun `どちらも閾値以上なら警告しない`() = assertWarning(deviceVolume = 21, soundVolume = 71, expected = null)

    @Test
    fun `無効のときnull`() = assertWarning(deviceVolume = 20, soundVolume = 20, expected = null, enabled = false)

    @Test
    fun `無効から有効にすると警告が出る`() =
        runTest {
            val enabled = MutableStateFlow(false)
            every { deviceRepository.observeVolume() } returns MutableStateFlow(19)
            every { soundRepository.volume() } returns MutableStateFlow(70)
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
            assertEquals(listOf(null, LowVolumeWarning(19, null)), result)
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

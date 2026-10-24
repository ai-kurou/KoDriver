package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DismissedVolumes
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.LowVolumeWarningDismissalRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLowVolumeWarningUseCaseTest {
    private val deviceRepository: DeviceVolumeRepository = mockk()
    private val soundRepository: SoundVolumePreferencesRepository = mockk()
    private val dismissalRepository: LowVolumeWarningDismissalRepository = mockk()

    @Test
    fun `端末音量だけが低い場合に警告する`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(20)
            every { soundRepository.volume() } returns MutableStateFlow(80)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes())
            assertEquals(LowVolumeWarning(20, null), useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `アプリ音量だけが低い場合に警告する`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(30)
            every { soundRepository.volume() } returns MutableStateFlow(79)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes())
            assertEquals(LowVolumeWarning(null, 79), useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `両方低い場合に両方の値を警告する`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(29)
            every { soundRepository.volume() } returns MutableStateFlow(79)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes())
            assertEquals(LowVolumeWarning(29, 79), useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `どちらも閾値以上なら警告しない`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(30)
            every { soundRepository.volume() } returns MutableStateFlow(80)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes())
            assertEquals(null, useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `閉じた音量以上では警告を抑制する`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(20)
            every { soundRepository.volume() } returns MutableStateFlow(60)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes(20, 60))
            assertEquals(null, useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `閉じた音量より下がると再表示する`() =
        runTest {
            every { deviceRepository.observeVolume() } returns MutableStateFlow(19)
            every { soundRepository.volume() } returns MutableStateFlow(59)
            every { dismissalRepository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes(20, 60))
            assertEquals(LowVolumeWarning(19, 59), useCase()().first())
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `基準以上への回復時にその側の記憶をクリアする`() =
        runTest {
            val dismissed = MutableStateFlow(DismissedVolumes(25, 60))
            every { deviceRepository.observeVolume() } returns MutableStateFlow(20)
            every { soundRepository.volume() } returns MutableStateFlow(80)
            every { dismissalRepository.observeDismissedVolumes() } returns dismissed
            coEvery { dismissalRepository.saveDismissedVolumes(DismissedVolumes(25, null)) } answers {
                dismissed.update { DismissedVolumes(25, null) }
            }

            assertEquals(LowVolumeWarning(20, null), useCase()().first())
            coVerify(exactly = 1) { dismissalRepository.saveDismissedVolumes(DismissedVolumes(25, null)) }
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    @Test
    fun `回復後の再低下ではクリア済みの記憶で警告する`() =
        runTest {
            val device = MutableStateFlow(20)
            val dismissed = MutableStateFlow(DismissedVolumes(20, 60))
            every { deviceRepository.observeVolume() } returns device
            every { soundRepository.volume() } returns MutableStateFlow(60)
            every { dismissalRepository.observeDismissedVolumes() } returns dismissed
            coEvery { dismissalRepository.saveDismissedVolumes(DismissedVolumes(null, 60)) } answers {
                dismissed.update { DismissedVolumes(null, 60) }
            }
            val warnings = useCase()()

            assertEquals(null, warnings.first())
            device.update { 50 }
            assertEquals(null, warnings.first())
            device.update { 25 }
            assertEquals(LowVolumeWarning(25, null), warnings.first())

            coVerify(exactly = 1) { dismissalRepository.saveDismissedVolumes(DismissedVolumes(null, 60)) }
            verifyInputs()
            confirmVerified(deviceRepository, soundRepository, dismissalRepository)
        }

    private fun useCase() =
        ObserveLowVolumeWarningUseCase(
            ObserveDeviceVolumeUseCase(deviceRepository),
            ObserveSoundVolumeUseCase(soundRepository),
            dismissalRepository,
        )

    private fun verifyInputs() {
        verify(exactly = 1) { deviceRepository.observeVolume() }
        verify(exactly = 1) { soundRepository.volume() }
        verify(exactly = 1) { dismissalRepository.observeDismissedVolumes() }
    }
}

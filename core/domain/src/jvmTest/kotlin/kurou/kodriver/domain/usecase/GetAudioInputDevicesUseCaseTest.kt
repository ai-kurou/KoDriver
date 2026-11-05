package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAudioInputDevicesUseCaseTest {
    private val repository: AudioInputDeviceRepository = mockk()

    @Test
    fun `Repositoryのデバイス一覧をそのまま返す`() =
        runTest {
            val devices = listOf(AudioInputDevice("マイク A"), AudioInputDevice("マイク B"))
            coEvery { repository.getDevices() } returns devices

            assertEquals(devices, GetAudioInputDevicesUseCase(repository)())
            coVerify(exactly = 1) { repository.getDevices() }
            confirmVerified(repository)
        }
}

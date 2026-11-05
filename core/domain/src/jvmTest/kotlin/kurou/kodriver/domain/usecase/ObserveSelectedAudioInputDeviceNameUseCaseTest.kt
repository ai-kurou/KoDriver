package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSelectedAudioInputDeviceNameUseCaseTest {
    private val repository: SelectedAudioInputDevicePreferencesRepository = mockk()

    @Test
    fun `保存済みのデバイス名を返す`() =
        runTest {
            every { repository.selectedDeviceName() } returns flowOf("Mic A")

            assertEquals("Mic A", ObserveSelectedAudioInputDeviceNameUseCase(repository)().first())

            verify(exactly = 1) { repository.selectedDeviceName() }
            confirmVerified(repository)
        }
}

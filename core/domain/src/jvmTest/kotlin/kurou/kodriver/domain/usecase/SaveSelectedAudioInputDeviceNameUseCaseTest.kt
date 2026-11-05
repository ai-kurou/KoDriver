package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kotlin.test.Test

class SaveSelectedAudioInputDeviceNameUseCaseTest {
    private val repository: SelectedAudioInputDevicePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `デバイス名を保存できる`() =
        runTest {
            SaveSelectedAudioInputDeviceNameUseCase(repository)("Mic A")

            coVerify(exactly = 1) { repository.saveSelectedDeviceName("Mic A") }
            confirmVerified(repository)
        }
}

package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(repository)("  左注意  ")

            coVerify(exactly = 1) { repository.saveSustainedLeftReadoutText("左注意") }
            confirmVerified(repository)
        }
}

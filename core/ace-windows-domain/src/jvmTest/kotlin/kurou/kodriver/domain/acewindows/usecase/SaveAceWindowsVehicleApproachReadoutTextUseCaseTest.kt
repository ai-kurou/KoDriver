package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsVehicleApproachReadoutTextUseCaseTest {
    private val repository: AceWindowsVehicleApproachPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsVehicleApproachReadoutTextUseCase(repository)("  車両接近  ")

            coVerify(exactly = 1) { repository.saveReadoutText("車両接近") }
            confirmVerified(repository)
        }
}

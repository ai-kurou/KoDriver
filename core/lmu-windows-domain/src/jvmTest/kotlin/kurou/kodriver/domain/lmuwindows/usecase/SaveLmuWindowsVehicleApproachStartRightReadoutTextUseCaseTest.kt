package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(repository)("  右注意  ")

            coVerify(exactly = 1) { repository.saveStartRightReadoutText("右注意") }
            confirmVerified(repository)
        }
}

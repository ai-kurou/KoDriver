package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(repository)("  オーバーヒート  ")

            coVerify(exactly = 1) { repository.saveOverheatReadoutText("オーバーヒート") }
            confirmVerified(repository)
        }
}

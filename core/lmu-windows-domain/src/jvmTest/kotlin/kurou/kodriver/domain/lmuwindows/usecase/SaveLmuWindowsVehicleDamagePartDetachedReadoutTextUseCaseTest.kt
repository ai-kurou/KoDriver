package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)("  部品脱落  ")

            coVerify(exactly = 1) { repository.savePartDetachedReadoutText("部品脱落") }
            confirmVerified(repository)
        }
}

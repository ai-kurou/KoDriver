package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(repository)("  タイヤ脱落  ")

            coVerify(exactly = 1) { repository.saveTyreDetachedReadoutText("タイヤ脱落") }
            confirmVerified(repository)
        }
}

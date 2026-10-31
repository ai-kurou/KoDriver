package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelLapsReadoutTextUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase(repository)("  残り{laps}周  ")

            coVerify(exactly = 1) { repository.saveReadoutText("残り{laps}周") }
            confirmVerified(repository)
        }
}

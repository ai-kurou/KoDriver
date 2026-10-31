package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(repository)("  燃料なし  ")

            coVerify(exactly = 1) { repository.saveEmptyReadoutText("燃料なし") }
            confirmVerified(repository)
        }
}

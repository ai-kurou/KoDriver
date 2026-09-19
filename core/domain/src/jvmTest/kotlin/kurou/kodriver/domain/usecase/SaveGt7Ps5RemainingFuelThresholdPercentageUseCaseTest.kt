package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelThresholdPercentageUseCaseTest {
    private val repository: Gt7Ps5RemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `燃料残量閾値を保存する`() =
        runTest {
            SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository)(45)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
            confirmVerified(repository)
        }
}

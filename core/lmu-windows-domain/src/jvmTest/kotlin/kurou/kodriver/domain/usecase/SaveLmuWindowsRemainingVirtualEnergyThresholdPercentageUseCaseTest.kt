package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCaseTest {
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(repository)(20)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(20) }
            confirmVerified(repository)
        }
}

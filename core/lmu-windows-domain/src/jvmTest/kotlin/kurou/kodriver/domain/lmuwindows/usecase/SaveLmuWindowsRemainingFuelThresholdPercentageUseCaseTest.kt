package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRemainingFuelThresholdPercentageUseCaseTest {
    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveLmuWindowsRemainingFuelThresholdPercentageUseCase(repository)(20)
            coVerify(exactly = 1) { repository.saveThresholdPercentage(20) }
            confirmVerified(repository)
        }
}

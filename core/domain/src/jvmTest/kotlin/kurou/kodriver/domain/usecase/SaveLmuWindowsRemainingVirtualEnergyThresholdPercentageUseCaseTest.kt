package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(repository)(20)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(20) }
            confirmVerified(repository)
        }
}

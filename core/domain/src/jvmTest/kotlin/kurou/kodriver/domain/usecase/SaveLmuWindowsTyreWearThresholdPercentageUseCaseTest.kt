package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreWearThresholdPercentageUseCaseTest {
    private val repository: LmuWindowsTyreWearPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `任意の値を保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsTyreWearThresholdPercentageUseCase(repository)

            useCase(30)
            useCase(60)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
            coVerify(exactly = 1) { repository.saveThresholdPercentage(60) }
            confirmVerified(repository)
        }
}

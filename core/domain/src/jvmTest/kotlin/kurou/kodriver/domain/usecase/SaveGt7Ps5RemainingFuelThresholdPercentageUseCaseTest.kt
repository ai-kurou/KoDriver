package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelThresholdPercentageUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5RemainingFuelPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `燃料残量閾値を保存する`() = runBlocking {
        SaveGt7Ps5RemainingFuelThresholdPercentageUseCase(repository)(45)

        coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
        confirmVerified(repository)
    }
}

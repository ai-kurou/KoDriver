package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `任意の値を保存できる`() =
        runBlocking {
            val useCase = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository)

            useCase(100)
            useCase(75)

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(100) }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(75) }
            confirmVerified(repository)
        }
}

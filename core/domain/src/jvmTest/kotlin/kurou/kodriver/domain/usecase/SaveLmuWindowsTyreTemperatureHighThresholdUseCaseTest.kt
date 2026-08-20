package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
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
        runTest {
            val useCase = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository)

            useCase(Celsius(100))
            useCase(Celsius(75))

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(100)) }
            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(75)) }
            confirmVerified(repository)
        }
}

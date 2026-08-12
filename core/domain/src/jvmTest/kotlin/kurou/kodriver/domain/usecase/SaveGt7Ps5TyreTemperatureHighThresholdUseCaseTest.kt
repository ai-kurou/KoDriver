package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveGt7Ps5TyreTemperatureHighThresholdUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5TyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `高温閾値を保存する`() =
        runTest {
            SaveGt7Ps5TyreTemperatureHighThresholdUseCase(repository)(95)

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(95) }
            confirmVerified(repository)
        }
}

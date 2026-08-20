package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureHighThresholdUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: AceWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `高温閾値を保存する`() =
        runTest {
            SaveAceWindowsTyreTemperatureHighThresholdUseCase(repository)(Celsius(90))

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(90)) }
            confirmVerified(repository)
        }
}

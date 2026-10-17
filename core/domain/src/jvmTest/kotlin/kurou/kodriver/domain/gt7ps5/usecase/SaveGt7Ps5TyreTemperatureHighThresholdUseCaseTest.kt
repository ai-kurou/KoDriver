package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test

class SaveGt7Ps5TyreTemperatureHighThresholdUseCaseTest {
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `高温閾値を保存する`() =
        runTest {
            SaveGt7Ps5TyreTemperatureHighThresholdUseCase(repository)(Celsius(95))

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(95)) }
            confirmVerified(repository)
        }
}

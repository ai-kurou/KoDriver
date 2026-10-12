package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

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

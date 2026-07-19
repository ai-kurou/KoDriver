package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {

    @Test
    fun `任意の値を保存できる`() = runBlocking {
        val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repository)

        useCase(100)
        useCase(75)

        coVerify(exactly = 1) { repository.saveHighThresholdCelsius(100) }
        coVerify(exactly = 1) { repository.saveHighThresholdCelsius(75) }
        confirmVerified(repository)
    }
}

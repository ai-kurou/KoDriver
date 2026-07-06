package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {

    private val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
    private val useCase = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

    @Test
    fun `任意の値を保存できる`() = runBlocking {
        useCase(100)
        assertEquals(100, repo.observeHighThresholdCelsius().first())

        useCase(75)
        assertEquals(75, repo.observeHighThresholdCelsius().first())
    }
}

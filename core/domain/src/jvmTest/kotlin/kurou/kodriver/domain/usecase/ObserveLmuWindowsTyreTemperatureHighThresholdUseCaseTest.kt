package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository(initialHighThreshold = 90)
        val useCase = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

        assertEquals(90, useCase().first())

        repo.saveHighThresholdCelsius(110)
        assertEquals(110, useCase().first())
    }
}

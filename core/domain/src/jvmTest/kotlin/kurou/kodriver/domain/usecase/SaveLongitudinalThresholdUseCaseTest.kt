package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLongitudinalThresholdUseCaseTest {

    @Test
    fun `保存した縦方向閾値がFlowに反映される`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository()
        val useCase = SaveLongitudinalThresholdUseCase(repo)

        useCase(50.0)
        assertEquals(50.0, repo.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository()
        val useCase = SaveLongitudinalThresholdUseCase(repo)

        useCase(50.0)
        useCase(30.0)
        assertEquals(30.0, repo.observeLongitudinalThresholdMeters().first())
    }
}

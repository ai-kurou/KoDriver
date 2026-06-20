package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLateralThresholdUseCaseTest {

    @Test
    fun `保存した横方向閾値がFlowに反映される`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository()
        val useCase = SaveLateralThresholdUseCase(repo)

        useCase(3.5)
        assertEquals(3.5, repo.observeLateralThresholdMeters().first())
    }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository()
        val useCase = SaveLateralThresholdUseCase(repo)

        useCase(3.5)
        useCase(1.0)
        assertEquals(1.0, repo.observeLateralThresholdMeters().first())
    }
}

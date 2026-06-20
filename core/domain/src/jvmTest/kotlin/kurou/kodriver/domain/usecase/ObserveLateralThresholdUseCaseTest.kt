package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLateralThresholdUseCaseTest {

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLateralThresholdUseCase(repo)

        assertEquals(2.0, useCase().first())
    }

    @Test
    fun `保存済みの横方向閾値をそのまま返す`() = runBlocking {
        val repo = FakeProximityThresholdsPreferencesRepository(initialLateral = 2.0)
        val useCase = ObserveLateralThresholdUseCase(repo)

        repo.saveLateralThresholdMeters(3.5)
        assertEquals(3.5, useCase().first())
    }
}

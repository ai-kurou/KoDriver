package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLongitudinalThresholdUseCaseTest {

    @Test
    fun `初期値を返す`() = runBlocking {
        val repo = FakeProximityThresholdsRepository(initialLongitudinal = 10.0)
        val useCase = ObserveLongitudinalThresholdUseCase(repo)

        assertEquals(10.0, useCase().first())
    }

    @Test
    fun `保存済みの縦方向閾値をそのまま返す`() = runBlocking {
        val repo = FakeProximityThresholdsRepository(initialLongitudinal = 10.0)
        val useCase = ObserveLongitudinalThresholdUseCase(repo)

        repo.saveLongitudinalThresholdMeters(50.0)
        assertEquals(50.0, useCase().first())
    }
}

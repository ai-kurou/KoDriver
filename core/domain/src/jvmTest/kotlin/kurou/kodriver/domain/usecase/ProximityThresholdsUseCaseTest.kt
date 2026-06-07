package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ProximityThresholdsUseCaseTest {

    @Test
    fun `縦方向閾値を保存するとFlowに反映され上書きで更新される`() = runBlocking {
        val repo = FakeProximityThresholdsRepository()
        val save = SaveLongitudinalThresholdUseCase(repo)
        val observe = ObserveLongitudinalThresholdUseCase(repo)

        save(50.0)
        assertEquals(50.0, observe().first())

        save(30.0)
        assertEquals(30.0, observe().first())
    }

    @Test
    fun `横方向閾値を保存するとFlowに反映され上書きで更新される`() = runBlocking {
        val repo = FakeProximityThresholdsRepository()
        val save = SaveLateralThresholdUseCase(repo)
        val observe = ObserveLateralThresholdUseCase(repo)

        save(3.5)
        assertEquals(3.5, observe().first())

        save(1.0)
        assertEquals(1.0, observe().first())
    }

    @Test
    fun `縦横の閾値は独立して保持される`() = runBlocking {
        val repo = FakeProximityThresholdsRepository()
        val saveLongitudinal = SaveLongitudinalThresholdUseCase(repo)
        val saveLateral = SaveLateralThresholdUseCase(repo)
        val observeLongitudinal = ObserveLongitudinalThresholdUseCase(repo)
        val observeLateral = ObserveLateralThresholdUseCase(repo)

        saveLongitudinal(40.0)
        saveLateral(5.0)

        assertEquals(40.0, observeLongitudinal().first())
        assertEquals(5.0, observeLateral().first())
    }
}

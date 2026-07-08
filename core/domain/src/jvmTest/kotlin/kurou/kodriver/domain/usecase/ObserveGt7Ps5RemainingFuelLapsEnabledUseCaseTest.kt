package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveGt7Ps5RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `保存済みの有効状態を返す`() = runBlocking {
        val repository = FakeGt7Ps5RemainingFuelLapsEnabledRepository(initialEnabled = false)
        val useCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        assertFalse(useCase().first())
    }

    @Test
    fun `未設定の場合はtrueを返す`() = runBlocking {
        val repository = FakeGt7Ps5RemainingFuelLapsEnabledRepository(initialEnabled = null)
        val useCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        assertTrue(useCase().first())
    }
}

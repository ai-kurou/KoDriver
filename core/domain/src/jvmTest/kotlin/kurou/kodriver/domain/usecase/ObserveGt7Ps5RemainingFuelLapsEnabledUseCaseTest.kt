package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse

class ObserveGt7Ps5RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `保存済みの有効状態を返す`() = runBlocking {
        val repository = FakeGt7Ps5RemainingFuelLapsPreferencesRepository(initialEnabled = false)
        val useCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        assertFalse(useCase().first())
    }
}

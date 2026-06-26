package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse

class SaveGt7Ps5RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = FakeGt7Ps5RemainingFuelLapsEnabledRepository()
        val saveUseCase = SaveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)
        val observeUseCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        saveUseCase(false)

        assertFalse(observeUseCase().first())
    }
}

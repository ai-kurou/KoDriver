package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse

class SaveGt7RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = FakeGt7RemainingFuelLapsPreferencesRepository()
        val saveUseCase = SaveGt7RemainingFuelLapsEnabledUseCase(repository)
        val observeUseCase = ObserveGt7RemainingFuelLapsEnabledUseCase(repository)

        saveUseCase(false)

        assertFalse(observeUseCase().first())
    }
}

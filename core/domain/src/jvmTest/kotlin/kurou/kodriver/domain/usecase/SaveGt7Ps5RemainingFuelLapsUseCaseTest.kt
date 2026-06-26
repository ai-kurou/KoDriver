package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveGt7Ps5RemainingFuelLapsUseCaseTest {

    @Test
    fun `燃料残り周回数を保存できる`() = runBlocking {
        val repository = FakeGt7Ps5RemainingFuelLapsPreferencesRepository()
        val saveUseCase = SaveGt7Ps5RemainingFuelLapsUseCase(repository)

        saveUseCase(1)

        assertEquals(1, repository.observeRemainingFuelLaps().first())
    }
}

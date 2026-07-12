package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createGt7Ps5RemainingFuelLapsPreferencesRepository(
    initial: Int = 3,
): Gt7Ps5RemainingFuelLapsPreferencesRepository {
    val repository = mockk<Gt7Ps5RemainingFuelLapsPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeRemainingFuelLaps() } returns state
    coEvery { repository.saveRemainingFuelLaps(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveGt7Ps5RemainingFuelLapsUseCaseTest {

    @Test
    fun `燃料残り周回数を保存できる`() = runBlocking {
        val repository = createGt7Ps5RemainingFuelLapsPreferencesRepository()
        val saveUseCase = SaveGt7Ps5RemainingFuelLapsUseCase(repository)

        saveUseCase(1)

        assertEquals(1, repository.observeRemainingFuelLaps().first())
    }
}

package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kotlin.test.Test
import kotlin.test.assertFalse

private fun createGt7Ps5RemainingFuelLapsEnabledRepository(
    initial: Boolean? = null,
): Gt7Ps5RemainingFuelLapsEnabledRepository {
    val repository = mockk<Gt7Ps5RemainingFuelLapsEnabledRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeEnabled() } returns state
    coEvery { repository.saveEnabled(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveGt7Ps5RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `有効状態を保存できる`() = runBlocking {
        val repository = createGt7Ps5RemainingFuelLapsEnabledRepository()
        val saveUseCase = SaveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)
        val observeUseCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        saveUseCase(false)

        assertFalse(observeUseCase().first())
    }
}

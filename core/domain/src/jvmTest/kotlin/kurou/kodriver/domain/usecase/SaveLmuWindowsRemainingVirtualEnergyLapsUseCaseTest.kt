package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsRemainingVirtualEnergyLapsPreferencesRepository(
    initial: Int = 3,
): LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository {
    val repository = mockk<LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeRemainingVirtualEnergyLaps() } returns state
    coEvery { repository.saveRemainingVirtualEnergyLaps(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveLmuWindowsRemainingVirtualEnergyLapsUseCaseTest {

    @Test
    fun `バーチャルエナジー残り周回数を保存できる`() = runBlocking {
        val repository = createLmuWindowsRemainingVirtualEnergyLapsPreferencesRepository()
        val saveUseCase = SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository)

        saveUseCase(1)

        assertEquals(1, repository.observeRemainingVirtualEnergyLaps().first())
    }
}

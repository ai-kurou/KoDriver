package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRemainingVirtualEnergyLapsUseCaseTest {

    @Test
    fun `バーチャルエナジー残り周回数を監視できる`() = runBlocking {
        val repository = mockk<LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository>()
        every { repository.observeRemainingVirtualEnergyLaps() } returns MutableStateFlow(5)
        val useCase = ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository)

        assertEquals(5, useCase().first())
    }
}

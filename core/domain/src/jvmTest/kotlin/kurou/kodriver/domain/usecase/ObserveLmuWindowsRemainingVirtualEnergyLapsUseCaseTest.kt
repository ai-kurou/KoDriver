package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsRemainingVirtualEnergyLapsUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `バーチャルエナジー残り周回数を監視できる`() = runBlocking {
        every { repository.observeRemainingVirtualEnergyLaps() } returns MutableStateFlow(5)
        val useCase = ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository)

        assertEquals(5, useCase().first())
        verify(exactly = 1) { repository.observeRemainingVirtualEnergyLaps() }
        confirmVerified(repository)
    }
}

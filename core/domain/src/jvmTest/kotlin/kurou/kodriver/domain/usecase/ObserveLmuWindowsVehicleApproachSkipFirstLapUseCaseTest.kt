package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存されたskipFirstLapがtrueのときtrueを返す`() = runBlocking {
        every { repository.observeSkipFirstLap() } returns MutableStateFlow(true)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertTrue(useCase().first())
        verify(exactly = 1) { repository.observeSkipFirstLap() }
        confirmVerified(repository)
    }

    @Test
    fun `保存されたskipFirstLapがfalseのときfalseを返す`() = runBlocking {
        every { repository.observeSkipFirstLap() } returns MutableStateFlow(false)
        val useCase = ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)

        assertFalse(useCase().first())
        verify(exactly = 1) { repository.observeSkipFirstLap() }
        confirmVerified(repository)
    }
}

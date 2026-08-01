package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsPitTimingTyreWearLapsUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsPitTimingPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `タイヤ摩耗予想残り周回数を監視できる`() =
        runBlocking {
            every { repository.observeTyreWearLaps() } returns MutableStateFlow(2)
            val useCase = ObserveLmuWindowsPitTimingTyreWearLapsUseCase(repository)

            assertEquals(2, useCase().first())
            verify(exactly = 1) { repository.observeTyreWearLaps() }
            confirmVerified(repository)
        }
}

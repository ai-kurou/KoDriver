package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5RemainingFuelLapsUseCaseTest {

    @Test
    fun `燃料残り周回数を監視できる`() = runBlocking {
        val useCase = ObserveGt7Ps5RemainingFuelLapsUseCase(
            FakeGt7Ps5RemainingFuelLapsPreferencesRepository(initialRemainingFuelLaps = 5),
        )

        assertEquals(5, useCase().first())
    }
}

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5TyreTemperatureHighThresholdUseCaseTest {
    @MockK
    private lateinit var repository: Gt7Ps5TyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `リポジトリの高温閾値を返す`() =
        runTest {
            val threshold = MutableStateFlow(Celsius(95))
            every { repository.observeHighThresholdCelsius() } returns threshold
            val useCase = ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(repository)

            assertEquals(Celsius(95), useCase().first())
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            confirmVerified(repository)
        }
}

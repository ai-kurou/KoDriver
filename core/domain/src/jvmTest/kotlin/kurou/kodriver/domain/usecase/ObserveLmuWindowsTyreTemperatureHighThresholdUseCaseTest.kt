package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(Celsius(90))
            every { repo.observeHighThresholdCelsius() } returns state
            coEvery { repo.saveHighThresholdCelsius(Celsius(110)) } answers { state.update { Celsius(110) } }
            val useCase = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

            assertEquals(Celsius(90), useCase().first())

            repo.saveHighThresholdCelsius(Celsius(110))
            assertEquals(Celsius(110), useCase().first())

            verify(exactly = 2) { repo.observeHighThresholdCelsius() }
            coVerify(exactly = 1) { repo.saveHighThresholdCelsius(Celsius(110)) }
            confirmVerified(repo)
        }
}

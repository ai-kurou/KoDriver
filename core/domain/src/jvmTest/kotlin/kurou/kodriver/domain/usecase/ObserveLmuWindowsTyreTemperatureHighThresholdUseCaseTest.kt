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
import kotlinx.coroutines.runBlocking
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
        runBlocking {
        val state = MutableStateFlow(90)
        every { repo.observeHighThresholdCelsius() } returns state
        coEvery { repo.saveHighThresholdCelsius(110) } answers { state.update { 110 } }
        val useCase = ObserveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

        assertEquals(90, useCase().first())

        repo.saveHighThresholdCelsius(110)
        assertEquals(110, useCase().first())

        verify(exactly = 2) { repo.observeHighThresholdCelsius() }
        coVerify(exactly = 1) { repo.saveHighThresholdCelsius(110) }
        confirmVerified(repo)
    }
}

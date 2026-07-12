package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
): LmuWindowsTyreTemperaturePreferencesRepository {
    val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
    val state = MutableStateFlow(initialHighThreshold)
    every { repository.observeHighThresholdCelsius() } returns state
    coEvery { repository.saveHighThresholdCelsius(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveLmuWindowsTyreTemperatureHighThresholdUseCaseTest {

    private val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
    private val useCase = SaveLmuWindowsTyreTemperatureHighThresholdUseCase(repo)

    @Test
    fun `任意の値を保存できる`() = runBlocking {
        useCase(100)
        assertEquals(100, repo.observeHighThresholdCelsius().first())

        useCase(75)
        assertEquals(75, repo.observeHighThresholdCelsius().first())
    }
}

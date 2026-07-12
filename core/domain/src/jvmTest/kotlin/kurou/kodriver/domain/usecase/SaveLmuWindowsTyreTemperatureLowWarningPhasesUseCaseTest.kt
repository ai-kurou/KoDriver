package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningSelectablePhases
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(): LmuWindowsTyreTemperaturePreferencesRepository {
    val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
    val state = MutableStateFlow<Map<SessionPhase, Boolean>>(emptyMap())
    every { repository.observeLowWarningPhases() } returns state
    coEvery { repository.saveLowWarningPhases(any()) } answers {
        val phases = firstArg<Set<SessionPhase>>()
        state.update {
            lmuWindowsTyreTemperatureLowWarningSelectablePhases.associateWith { phase -> phase in phases }
        }
    }
    return repository
}

class SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {

    private val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
    private val useCase = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

    @Test
    fun `任意のフェーズ集合を保存できる`() = runBlocking {
        useCase(setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION))
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to true,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to true,
            ),
            repo.observeLowWarningPhases().first(),
        )

        useCase(emptySet())
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to false,
            ),
            repo.observeLowWarningPhases().first(),
        )
    }
}

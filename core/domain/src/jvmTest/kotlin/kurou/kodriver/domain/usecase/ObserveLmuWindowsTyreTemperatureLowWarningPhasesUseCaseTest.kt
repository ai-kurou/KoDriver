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

private fun createLmuWindowsTyreTemperaturePreferencesRepository(
    initialLowWarningPhases: Map<SessionPhase, Boolean> = emptyMap(),
): LmuWindowsTyreTemperaturePreferencesRepository {
    val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
    val lowWarningPhases = MutableStateFlow(initialLowWarningPhases)
    every { repository.observeLowWarningPhases() } returns lowWarningPhases
    listOf(setOf(SessionPhase.GARAGE)).forEach { phases ->
        coEvery { repository.saveLowWarningPhases(phases) } answers {
            lowWarningPhases.update {
                lmuWindowsTyreTemperatureLowWarningSelectablePhases.associateWith { phase -> phase in phases }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

        assertEquals(
            setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
            useCase().first(),
        )

        repo.saveLowWarningPhases(setOf(SessionPhase.GARAGE))
        assertEquals(setOf(SessionPhase.GARAGE), useCase().first())
        io.mockk.coVerify(exactly = 1) { repo.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) }
        io.mockk.verify(exactly = 2) { repo.observeLowWarningPhases() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `一部のフェーズだけ保存されている場合は保存済みの値がデフォルトより優先される`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository(
            initialLowWarningPhases = mapOf(SessionPhase.GARAGE to true),
        )
        val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

        assertEquals(
            setOf(SessionPhase.GARAGE, SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
            useCase().first(),
        )
        io.mockk.verify(exactly = 1) { repo.observeLowWarningPhases() }
        io.mockk.confirmVerified(repo)
    }
}

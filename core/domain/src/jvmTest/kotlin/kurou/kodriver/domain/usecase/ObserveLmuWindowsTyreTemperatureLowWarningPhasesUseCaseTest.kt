package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.SessionPhase
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {

    @Test
    fun `初期値を返す・保存済みの値を返す`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

        assertEquals(
            setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
            useCase().first(),
        )

        repo.saveLowWarningPhases(setOf(SessionPhase.GARAGE))
        assertEquals(setOf(SessionPhase.GARAGE), useCase().first())
    }

    @Test
    fun `一部のフェーズだけ保存されている場合は保存済みの値がデフォルトより優先される`() = runBlocking {
        val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository(
            initialLowWarningPhases = mapOf(SessionPhase.GARAGE to true),
        )
        val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

        assertEquals(
            setOf(SessionPhase.GARAGE, SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
            useCase().first(),
        )
    }
}

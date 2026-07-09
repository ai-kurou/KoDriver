package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.SessionPhase
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {

    private val repo = FakeLmuWindowsTyreTemperaturePreferencesRepository()
    private val useCase = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

    @Test
    fun `任意のフェーズ集合を保存できる`() = runBlocking {
        useCase(setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION))
        assertEquals(setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION), repo.observeLowWarningPhases().first())

        useCase(emptySet())
        assertEquals(emptySet(), repo.observeLowWarningPhases().first())
    }
}

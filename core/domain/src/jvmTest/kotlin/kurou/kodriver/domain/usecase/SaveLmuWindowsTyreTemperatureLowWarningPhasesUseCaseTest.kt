package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {

    @Test
    fun `任意のフェーズ集合を保存できる`() = runBlocking {
        val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>(relaxUnitFun = true)
        val useCase = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository)
        val phases = setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION)

        useCase(phases)
        useCase(emptySet())

        coVerify(exactly = 1) { repository.saveLowWarningPhases(phases) }
        coVerify(exactly = 1) { repository.saveLowWarningPhases(emptySet()) }
        confirmVerified(repository)
    }
}

package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    suspend operator fun invoke(phases: Set<SessionPhase>) = repository.saveLowWarningPhases(phases)
}

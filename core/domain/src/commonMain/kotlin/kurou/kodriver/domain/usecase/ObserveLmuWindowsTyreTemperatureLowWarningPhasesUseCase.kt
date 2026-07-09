package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Set<SessionPhase>> = repository.observeLowWarningPhases()
}

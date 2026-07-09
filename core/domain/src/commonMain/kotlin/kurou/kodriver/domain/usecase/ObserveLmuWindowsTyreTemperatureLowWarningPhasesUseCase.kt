package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

// detailPane（LmuWindowsReadoutTyreTemperatureDetailViewModel）が参照するデフォルト有効フェーズを
// この一箇所にのみ定義する。
private val lowWarningPhaseDefaults: Map<SessionPhase, Boolean> = mapOf(
    SessionPhase.GARAGE to true,
    SessionPhase.WARM_UP to true,
    SessionPhase.GRID_WALK to true,
    SessionPhase.FORMATION to true,
)

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Set<SessionPhase>> =
        repository.observeLowWarningPhases().map { persisted ->
            (lowWarningPhaseDefaults + persisted).filterValues { enabled -> enabled }.keys
        }
}

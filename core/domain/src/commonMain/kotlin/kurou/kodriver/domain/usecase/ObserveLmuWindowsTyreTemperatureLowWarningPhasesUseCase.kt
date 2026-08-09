package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.SessionPhase
import kurou.kodriver.core.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.core.model.lmuWindowsTyreTemperatureLowWarningSelectablePhases
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

// 選択可能なフェーズのうちGARAGEのみデフォルトで無効(false)とする。選択可能なフェーズ自体の定義は
// lmuWindowsTyreTemperatureLowWarningSelectablePhases に一元化している。
private val lowWarningPhaseDefaults: Map<SessionPhase, Boolean> =
    lmuWindowsTyreTemperatureLowWarningSelectablePhases.associateWith { phase ->
        phase in lmuWindowsTyreTemperatureLowWarningDefaultPhases
    }

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Set<SessionPhase>> =
        repository.observeLowWarningPhases().map { persisted ->
            (lowWarningPhaseDefaults + persisted).filterValues { enabled -> enabled }.keys
        }
}

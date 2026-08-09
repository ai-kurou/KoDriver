package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.model.READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class ObserveReadoutEnabledStatesUseCase(
    private val repository: ReadoutPreferencesRepository,
) {
    operator fun invoke(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeReadoutEnabledStates(simulator).map { persisted ->
            val defaults = Simulator.fromId(simulator)?.let { READOUT_ENABLED_STATE_DEFAULT[it] }.orEmpty()
            defaults + persisted
        }
}

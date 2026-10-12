package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutCatalog
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class ObserveReadoutEnabledStatesUseCase(
    private val repository: ReadoutPreferencesRepository,
    private val catalog: ReadoutCatalog,
) {
    operator fun invoke(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeReadoutEnabledStates(simulator).map { persisted ->
            val defaults = Simulator.fromId(simulator)?.let { catalog.enabledStateDefaults(it) }.orEmpty()
            defaults + persisted
        }
}

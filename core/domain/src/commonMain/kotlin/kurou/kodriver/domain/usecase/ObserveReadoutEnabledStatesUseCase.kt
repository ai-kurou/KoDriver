package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

// listPane（ReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照できるよう、シミュレーターごとのデフォルト有効状態をこの一箇所にのみ定義する。
private val readoutEnabledStateDefaults: Map<Simulator, Map<ReadoutItemKey, Boolean>> = mapOf(
    Simulator.LmuWindows to mapOf(
        ReadoutItemKey.TyreTemperature to false,
        ReadoutItemKey.MyBestLap to false,
    ),
)

class ObserveReadoutEnabledStatesUseCase(private val repository: ReadoutPreferencesRepository) {
    operator fun invoke(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeReadoutEnabledStates(simulator).map { persisted ->
            val defaults = Simulator.fromId(simulator)?.let { readoutEnabledStateDefaults[it] }.orEmpty()
            defaults + persisted
        }
}

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

// listPane（ReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照できるよう、シミュレーターごとのデフォルト有効状態をこの一箇所にのみ定義する。
// listPaneに表示される ReadoutItemKey は必ずここに列挙すること（省略＝デフォルトtrue、ではない）。
private val readoutEnabledStateDefaults: Map<Simulator, Map<ReadoutItemKey, Boolean>> = mapOf(
    Simulator.LmuWindows to mapOf(
        ReadoutItemKey.LmuWindows.Flag to true,
        ReadoutItemKey.LmuWindows.VehicleApproach to true,
        ReadoutItemKey.LmuWindows.VehicleDamage to true,
        ReadoutItemKey.LmuWindows.TyreTemperature to false,
        ReadoutItemKey.LmuWindows.MyBestLap to false,
    ),
    Simulator.Gt7Ps5 to mapOf(
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps to true,
        ReadoutItemKey.Gt7Ps5.MyBestLap to true,
    ),
)

class ObserveReadoutEnabledStatesUseCase(private val repository: ReadoutPreferencesRepository) {
    operator fun invoke(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeReadoutEnabledStates(simulator).map { persisted ->
            val defaults = Simulator.fromId(simulator)?.let { readoutEnabledStateDefaults[it] }.orEmpty()
            defaults + persisted
        }
}

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
        ReadoutItemKey.LmuWindows.Flag.Root to true,
        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
        ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
        ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
        ReadoutItemKey.LmuWindows.PitTiming.Root to true,
        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to false,
        ReadoutItemKey.LmuWindows.TyreWear.Root to false,
        ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
    ),
    Simulator.Gt7Ps5 to mapOf(
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
        ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
    ),
    Simulator.AceWindows to mapOf(
        ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
    ),
)

class ObserveReadoutEnabledStatesUseCase(private val repository: ReadoutPreferencesRepository) {
    operator fun invoke(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeReadoutEnabledStates(simulator).map { persisted ->
            val defaults = Simulator.fromId(simulator)?.let { readoutEnabledStateDefaults[it] }.orEmpty()
            defaults + persisted
        }
}

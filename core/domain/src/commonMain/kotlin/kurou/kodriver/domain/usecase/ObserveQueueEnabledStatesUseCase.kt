package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository

// listPane（ReadoutListViewModel）が参照するデフォルト値をこの一箇所にのみ定義する。
// supportsQueue が true の ReadoutItemKey.TopLevel は必ずここに列挙すること（省略＝デフォルトfalse、ではない）。
private val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(
    ReadoutItemKey.LmuWindows.Flag.Root to false,
    ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
    ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
    ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root to false,
    ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false,
)

class ObserveQueueEnabledStatesUseCase(private val repository: QueuePreferencesRepository) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeQueueEnabledStates().map { persisted -> queueEnabledStateDefaults + persisted }
}

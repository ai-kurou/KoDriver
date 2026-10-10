package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

// detailPane（LmuWindowsReadoutVehicleApproachDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val vehicleApproachEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
        LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false,
    )

class ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> vehicleApproachEnabledStateDefaults + persisted }
}

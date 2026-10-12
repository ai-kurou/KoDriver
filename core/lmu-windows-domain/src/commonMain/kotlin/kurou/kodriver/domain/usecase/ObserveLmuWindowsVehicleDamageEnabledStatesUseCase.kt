package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

// detailPane（LmuWindowsReadoutVehicleDamageDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val vehicleDamageEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true,
        LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to true,
        LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to true,
    )

class ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> vehicleDamageEnabledStateDefaults + persisted }
}

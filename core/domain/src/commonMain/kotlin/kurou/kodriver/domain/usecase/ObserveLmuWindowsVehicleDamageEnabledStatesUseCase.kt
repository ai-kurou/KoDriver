package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository

// detailPane（LmuWindowsReadoutVehicleDamageDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val vehicleDamageEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true,
    )

class ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(
    private val repository: LmuWindowsVehicleDamagePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> vehicleDamageEnabledStateDefaults + persisted }
}

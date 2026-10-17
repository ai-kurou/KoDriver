package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

// detailPane（AceWindowsAutoReadoutVehicleApproachDetailViewModel）・Narrator（AceWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val vehicleApproachEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.VehicleApproach.StartReadout to
            ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT,
    )

class ObserveAceWindowsVehicleApproachEnabledStatesUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> vehicleApproachEnabledStateDefaults + persisted }
}

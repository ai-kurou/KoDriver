package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT

internal data class LmuWindowsReadoutVehicleDamageDetailUiState(
    val overheatEnabled: Boolean = true,
    val overheatReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT,
    val partDetachedEnabled: Boolean = true,
    val partDetachedReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT,
    val tyreDetachedEnabled: Boolean = true,
    val tyreDetachedReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
)

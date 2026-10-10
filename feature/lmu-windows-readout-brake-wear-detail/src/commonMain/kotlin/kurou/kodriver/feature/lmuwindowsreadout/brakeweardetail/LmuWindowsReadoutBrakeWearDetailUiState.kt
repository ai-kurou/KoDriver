package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

internal data class LmuWindowsReadoutBrakeWearDetailUiState(
    /** 現在のブレーキ残量。値を取得できていない間は null。 */
    val remaining: LmuWindowsBrakeWearRemainingData? = null,
    val vehicleClassLowThresholdPercent: Map<LmuWindowsVehicleClassData, Int> = emptyMap(),
    val selectedVehicleClass: LmuWindowsVehicleClassData = LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT,
    val readoutText: String = LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)

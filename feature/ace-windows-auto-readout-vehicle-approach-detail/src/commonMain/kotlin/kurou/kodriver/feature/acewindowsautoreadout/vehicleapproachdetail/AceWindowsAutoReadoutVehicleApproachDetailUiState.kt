package kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT

internal data class AceWindowsAutoReadoutVehicleApproachDetailUiState(
    val readoutText: String = ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val thresholdMeters: Double = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
    val startReadoutEnabled: Boolean = ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT,
)

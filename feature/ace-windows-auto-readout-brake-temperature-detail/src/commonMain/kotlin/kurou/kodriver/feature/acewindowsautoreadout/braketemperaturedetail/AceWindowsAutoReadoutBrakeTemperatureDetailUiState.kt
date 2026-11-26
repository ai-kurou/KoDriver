package kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT

internal data class AceWindowsAutoReadoutBrakeTemperatureDetailUiState(
    val overheatReadoutText: String = ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val overheatWarningEnabled: Boolean = true,
    val highThresholdCelsius: Int = ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
)

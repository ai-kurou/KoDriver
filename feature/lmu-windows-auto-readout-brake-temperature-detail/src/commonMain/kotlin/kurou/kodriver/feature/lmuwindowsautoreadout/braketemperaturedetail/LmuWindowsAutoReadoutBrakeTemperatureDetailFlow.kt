package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutBrakeTemperatureDetailFlow(
    vehicleClassHighThreshold: Flow<Map<LmuWindowsVehicleClassData, Int>>,
    vehicleClassSelection: Flow<LmuWindowsVehicleClassData>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutBrakeTemperatureDetailUiState> =
        combine(
            vehicleClassHighThreshold,
            vehicleClassSelection,
            enabledStates,
            readoutText,
            isTextToSpeechAvailable,
        ) { vehicleClassHighThresholdCelsius, selectedVehicleClass, enabledStates, text, available ->
            LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius,
                selectedVehicleClass = selectedVehicleClass,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout),
            )
        }
}

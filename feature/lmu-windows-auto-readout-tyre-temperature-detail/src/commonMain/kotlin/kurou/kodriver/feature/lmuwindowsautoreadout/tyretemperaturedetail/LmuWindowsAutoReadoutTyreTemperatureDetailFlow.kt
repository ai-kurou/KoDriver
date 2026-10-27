package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutTyreTemperatureDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    lowWarningPhases: Flow<Set<SessionPhase>>,
    vehicleClassHighThreshold: Flow<Map<LmuWindowsVehicleClassData, Celsius>>,
    vehicleClassSelection: Flow<LmuWindowsVehicleClassData>,
    overheatReadoutText: Flow<String>,
    coldReadoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    private val temperatureSettings =
        combine(
            enabledStates,
            lowWarningPhases,
            vehicleClassHighThreshold,
            vehicleClassSelection,
        ) { states, lowWarningPhases, vehicleClassHighThresholdCelsius, selectedVehicleClass ->
            LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning),
                lowWarningEnabled = states.getValue(LmuWindowsReadoutItemKey.TyreTemperature.LowWarning),
                lowWarningPhases = lowWarningPhases,
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius.mapValues { it.value.value },
                selectedVehicleClass = selectedVehicleClass,
            )
        }

    val uiState: Flow<LmuWindowsAutoReadoutTyreTemperatureDetailUiState> =
        combine(
            temperatureSettings,
            overheatReadoutText,
            coldReadoutText,
            isTextToSpeechAvailable,
        ) { settings, text, coldText, available ->
            settings.copy(overheatReadoutText = text, coldReadoutText = coldText, isTextToSpeechAvailable = available)
        }
}

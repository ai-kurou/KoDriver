package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutVehicleApproachDetailFlow(
    lateralThresholdMeters: Flow<Double>,
    longitudinalThresholdMeters: Flow<Double>,
    sustainedApproachDurationSeconds: Flow<Int>,
    skipFirstLap: Flow<Boolean>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutTextSettings: Flow<ReadoutTextSettingsUiState>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutVehicleApproachDetailUiState> =
        combine(
            combine(
                lateralThresholdMeters,
                longitudinalThresholdMeters,
                sustainedApproachDurationSeconds,
            ) { lateral, longitudinal, duration -> Triple(lateral, longitudinal, duration) },
            skipFirstLap,
            enabledStates,
            readoutTextSettings,
        ) { thresholdValues, skipFirstLap, enabledStates, textState ->
            val (lateral, longitudinal, duration) = thresholdValues
            LmuWindowsAutoReadoutVehicleApproachDetailUiState(
                lateralThresholdMeters = lateral,
                longitudinalThresholdMeters = longitudinal,
                sustainedApproachDurationSeconds = duration,
                skipFirstLap = skipFirstLap,
                startReadoutEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.VehicleApproach.StartReadout),
                startLeftText = textState.startLeftText,
                startRightText = textState.startRightText,
                sustainedReadoutEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.VehicleApproach.Sustained),
                sustainedLeftText = textState.sustainedLeftText,
                sustainedRightText = textState.sustainedRightText,
                isTextToSpeechAvailable = textState.isTextToSpeechAvailable,
            )
        }
}

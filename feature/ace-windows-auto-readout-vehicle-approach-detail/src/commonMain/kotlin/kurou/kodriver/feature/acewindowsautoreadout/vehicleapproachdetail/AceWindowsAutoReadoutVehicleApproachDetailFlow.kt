package kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class AceWindowsAutoReadoutVehicleApproachDetailFlow(
    thresholdMeters: Flow<Double>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<AceWindowsAutoReadoutVehicleApproachDetailUiState> =
        combine(
            thresholdMeters,
            enabledStates,
            readoutText,
            isTextToSpeechAvailable,
        ) { thresholdMeters, enabledStates, readoutText, isTextToSpeechAvailable ->
            AceWindowsAutoReadoutVehicleApproachDetailUiState(
                thresholdMeters = thresholdMeters,
                readoutText = readoutText,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
                startReadoutEnabled = enabledStates.getValue(AceWindowsReadoutItemKey.VehicleApproach.StartReadout),
            )
        }
}

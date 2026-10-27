package kurou.kodriver.feature.acewindowsautoreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class AceWindowsAutoReadoutTyreTemperatureDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    highThresholdCelsius: Flow<Celsius>,
    overheatReadoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<AceWindowsAutoReadoutTyreTemperatureDetailUiState> =
        combine(
            enabledStates,
            highThresholdCelsius,
            overheatReadoutText,
            isTextToSpeechAvailable,
        ) { states, highThresholdCelsius, text, available ->
            AceWindowsAutoReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius.value,
                overheatReadoutText = text,
                isTextToSpeechAvailable = available,
            )
        }
}

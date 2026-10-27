package kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class Gt7Ps5AutoReadoutTyreTemperatureDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    highThresholdCelsius: Flow<Celsius>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<Gt7Ps5AutoReadoutTyreTemperatureDetailUiState> =
        combine(
            enabledStates,
            highThresholdCelsius,
            readoutText,
            isTextToSpeechAvailable,
        ) { states, highThresholdCelsius, text, available ->
            Gt7Ps5AutoReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius.value,
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }
}

package kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutVehicleDamageDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    overheatReadoutText: Flow<String>,
    partDetachedReadoutText: Flow<String>,
    tyreDetachedReadoutText: Flow<String>,
    textToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutVehicleDamageDetailUiState> =
        combine(
            enabledStates,
            overheatReadoutText,
            partDetachedReadoutText,
            tyreDetachedReadoutText,
            textToSpeechAvailable,
        ) { states, overheatText, partDetachedText, tyreDetachedText, available ->
            LmuWindowsAutoReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(LmuWindowsReadoutItemKey.VehicleDamage.Overheat),
                partDetachedEnabled = states.getValue(LmuWindowsReadoutItemKey.VehicleDamage.PartDetached),
                tyreDetachedEnabled = states.getValue(LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached),
                overheatReadoutText = overheatText,
                partDetachedReadoutText = partDetachedText,
                tyreDetachedReadoutText = tyreDetachedText,
                isTextToSpeechAvailable = available,
            )
        }
}

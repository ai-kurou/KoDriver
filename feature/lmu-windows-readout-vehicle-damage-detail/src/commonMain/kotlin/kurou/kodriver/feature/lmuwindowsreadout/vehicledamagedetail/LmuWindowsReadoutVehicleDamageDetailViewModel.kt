package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsOverheatVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase

internal class LmuWindowsReadoutVehicleDamageDetailViewModel(
    observeEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
    observeOverheatVoiceType: ObserveLmuWindowsOverheatVoiceTypeUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleDamageEnabledStateUseCase,
    private val saveOverheatVoiceType: SaveLmuWindowsOverheatVoiceTypeUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> =
        combine(observeEnabledStates(), observeOverheatVoiceType()) { states, overheatVoiceType ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat),
                overheatVoiceType = overheatVoiceType,
                partDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutVehicleDamageDetailUiState(),
        )

    fun onOverheatEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, enabled) }
    }

    fun onOverheatVoiceTypeChanged(type: OverheatVoiceType) {
        viewModelScope.launch { saveOverheatVoiceType(type) }
    }

    fun onPreviewClicked(type: OverheatVoiceType) {
        playSpeechEvent(
            when (type) {
                OverheatVoiceType.GP2_GP2 -> SpeechEvent.Overheating
                OverheatVoiceType.STANDARD -> SpeechEvent.OverheatingStandard
            },
        )
    }

    fun onPartDetachedEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, enabled) }
    }

    fun onPartDetachedPreviewClicked() {
        playSpeechEvent(SpeechEvent.PartDetached)
    }
}

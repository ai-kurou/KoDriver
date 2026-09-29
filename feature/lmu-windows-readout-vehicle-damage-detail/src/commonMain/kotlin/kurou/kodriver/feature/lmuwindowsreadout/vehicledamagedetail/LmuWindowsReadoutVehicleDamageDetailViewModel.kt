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

internal data class VehicleDamageUseCases(
    val observeEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
    val observeOverheatVoiceType: ObserveLmuWindowsOverheatVoiceTypeUseCase,
    val saveEnabledState: SaveLmuWindowsVehicleDamageEnabledStateUseCase,
    val saveOverheatVoiceType: SaveLmuWindowsOverheatVoiceTypeUseCase,
)

internal class LmuWindowsReadoutVehicleDamageDetailViewModel(
    private val vehicleDamageUseCases: VehicleDamageUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> =
        combine(
            vehicleDamageUseCases.observeEnabledStates(),
            vehicleDamageUseCases.observeOverheatVoiceType(),
        ) { states, overheatVoiceType ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat),
                overheatVoiceType = overheatVoiceType,
                partDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached),
                tyreDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutVehicleDamageDetailUiState(),
        )

    fun onOverheatEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            vehicleDamageUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, enabled)
        }
    }

    fun onOverheatVoiceTypeChanged(type: OverheatVoiceType) {
        viewModelScope.launch { vehicleDamageUseCases.saveOverheatVoiceType(type) }
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
        viewModelScope.launch {
            vehicleDamageUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, enabled)
        }
    }

    fun onPartDetachedPreviewClicked() {
        playSpeechEvent(SpeechEvent.PartDetached)
    }

    fun onTyreDetachedEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            vehicleDamageUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached, enabled)
        }
    }

    fun onTyreDetachedPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreDetached)
    }
}

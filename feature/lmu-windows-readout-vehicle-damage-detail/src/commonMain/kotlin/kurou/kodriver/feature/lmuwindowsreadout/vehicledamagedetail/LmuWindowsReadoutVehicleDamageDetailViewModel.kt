package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutSpeechEventPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase

internal data class VehicleDamageUseCases(
    val observeEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
    val saveEnabledState: SaveLmuWindowsVehicleDamageEnabledStateUseCase,
    val observeOverheatReadoutText: ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase,
    val saveOverheatReadoutText: SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase,
    val observePartDetachedReadoutText: ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase,
    val savePartDetachedReadoutText: SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase,
    val observeTyreDetachedReadoutText: ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase,
    val saveTyreDetachedReadoutText: SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase,
)

internal data class VehicleDamageReadoutUseCases(
    val playSpeechEvent: PlaySpeechEventUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutVehicleDamageDetailViewModel(
    private val vehicleDamageUseCases: VehicleDamageUseCases,
    readout: VehicleDamageReadoutUseCases,
) : ViewModel() {
    private val previewHelper =
        ReadoutSpeechEventPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playSpeechEvent,
        )

    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> =
        combine(
            vehicleDamageUseCases.observeEnabledStates(),
            vehicleDamageUseCases.observeOverheatReadoutText(),
            vehicleDamageUseCases.observePartDetachedReadoutText(),
            vehicleDamageUseCases.observeTyreDetachedReadoutText(),
            previewHelper.textToSpeechAvailable,
        ) { states, overheatText, partDetachedText, tyreDetachedText, available ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat),
                partDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached),
                tyreDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached),
                overheatReadoutText = overheatText,
                partDetachedReadoutText = partDetachedText,
                tyreDetachedReadoutText = tyreDetachedText,
                isTextToSpeechAvailable = available,
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

    fun onOverheatReadoutTextChanged(text: String) {
        viewModelScope.launch { vehicleDamageUseCases.saveOverheatReadoutText(text) }
    }

    fun onOverheatReadoutTextPreviewClicked(text: String) {
        preview(SpeechEvent.Overheating(resolvedText = text), text)
    }

    fun onPartDetachedEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            vehicleDamageUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached, enabled)
        }
    }

    fun onPartDetachedReadoutTextChanged(text: String) {
        viewModelScope.launch { vehicleDamageUseCases.savePartDetachedReadoutText(text) }
    }

    fun onPartDetachedReadoutTextPreviewClicked(text: String) {
        preview(SpeechEvent.PartDetached(resolvedText = text), text)
    }

    fun onTyreDetachedEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            vehicleDamageUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.TyreDetached, enabled)
        }
    }

    fun onTyreDetachedReadoutTextChanged(text: String) {
        viewModelScope.launch { vehicleDamageUseCases.saveTyreDetachedReadoutText(text) }
    }

    fun onTyreDetachedReadoutTextPreviewClicked(text: String) {
        preview(SpeechEvent.TyreDetached(resolvedText = text), text)
    }

    /** 空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    private fun preview(
        event: SpeechEvent,
        text: String,
    ) {
        viewModelScope.launch {
            previewHelper.preview(text, event)
        }
    }
}

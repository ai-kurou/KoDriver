package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    // タイヤ脱落は永続化未対応のため、ViewModel内のみで保持する表示専用の状態
    private val tyreDetachedEnabled = MutableStateFlow(true)

    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> =
        combine(
            observeEnabledStates(),
            observeOverheatVoiceType(),
            tyreDetachedEnabled,
        ) { states, overheatVoiceType, tyreDetachedEnabled ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat),
                overheatVoiceType = overheatVoiceType,
                partDetachedEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.PartDetached),
                tyreDetachedEnabled = tyreDetachedEnabled,
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

    // タイヤ脱落は永続化・実際の音声再生とも未実装（表示のみ）
    fun onTyreDetachedEnabledChanged(enabled: Boolean) {
        tyreDetachedEnabled.update { enabled }
    }
}

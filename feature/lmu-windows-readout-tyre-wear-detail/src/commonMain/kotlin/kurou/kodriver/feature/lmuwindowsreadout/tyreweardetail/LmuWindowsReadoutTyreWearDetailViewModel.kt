package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal data class TyreWearUseCases(
    val observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveLmuWindowsTyreWearThresholdPercentageUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    val observeText: ObserveLmuWindowsTyreWearReadoutTextUseCase,
    val saveText: SaveLmuWindowsTyreWearReadoutTextUseCase,
)

internal data class TyreWearReadoutUseCases(
    val playSpeechEvent: PlaySpeechEventUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutTyreWearDetailViewModel(
    private val tyreWearUseCases: TyreWearUseCases,
    private val readout: TyreWearReadoutUseCases,
) : ViewModel() {
    private val textToSpeechAvailable =
        flow { emit(readout.checkTextToSpeechAvailable()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val uiState: StateFlow<LmuWindowsReadoutTyreWearDetailUiState> =
        combine(
            tyreWearUseCases.observeThresholdPercentage(),
            tyreWearUseCases.observeReadoutEnabledStates(Simulator.LmuWindows.id),
            tyreWearUseCases.observeText(),
            textToSpeechAvailable,
        ) { thresholdPercentage, enabledStates, text, available ->
            LmuWindowsReadoutTyreWearDetailUiState(
                thresholdPercentage = thresholdPercentage,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.TyreWear.WarningReadout),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreWearDetailUiState(),
        )

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { tyreWearUseCases.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val percentage = uiState.value.thresholdPercentage
        val formattedText = formatLmuWindowsTyreWearReadoutText(text, percentage)
        if (formattedText.isBlank() || !textToSpeechAvailable.value) return
        viewModelScope.launch {
            val volume = readout.observeSoundVolume().first()
            if (volume <= 0) return@launch
            readout.playSpeechEvent(
                SpeechEvent.TyreWearWarning(
                    percentage = percentage,
                    resolvedText = formattedText,
                ),
            )
        }
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { tyreWearUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            tyreWearUseCases.saveThresholdPercentage(
                LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreWearUseCases.saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.TyreWear.WarningReadout,
                enabled,
            )
        }
    }
}

package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal enum class VehicleApproachReadoutTextSlot {
    StartLeft,
    StartRight,
    SustainedLeft,
    SustainedRight,
}

internal data class ReadoutTextSettingsUiState(
    val startLeftText: String = LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT,
    val startRightText: String = LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT,
    val sustainedLeftText: String = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT,
    val sustainedRightText: String = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
)

@Suppress("LongParameterList")
internal data class ReadoutTextSettingsUseCases(
    val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    val observeSustainedLeftText: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase,
    val observeSustainedRightText: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase,
    val saveStartLeftText: SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase,
    val saveStartRightText: SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val speakText: SpeakTextUseCase,
)

/** 車両接近の左右別読み上げ文言の状態、保存、既定値への復帰、試聴を管理する。 */
@Suppress("LongParameterList")
internal class ReadoutTextSettingsStateHolder(
    private val scope: CoroutineScope,
    private val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    observeSustainedLeftText: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase,
    observeSustainedRightText: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase,
    private val saveStartLeftText: SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase,
    private val saveStartRightText: SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
    playStartSoundForKey: PlayStartSoundForKeyUseCase,
    speakText: SpeakTextUseCase,
) {
    private val preview =
        ReadoutTextPreviewHelper(
            scope,
            checkTextToSpeechAvailable,
            observeSoundVolume,
            playStartSoundForKey,
            speakText,
        )

    val uiState: StateFlow<ReadoutTextSettingsUiState> =
        combine(
            vehicleApproachPreferences.observeStartLeftReadoutText(),
            vehicleApproachPreferences.observeStartRightReadoutText(),
            observeSustainedLeftText(),
            observeSustainedRightText(),
            preview.textToSpeechAvailable,
        ) { startLeft, startRight, sustainedLeft, sustainedRight, available ->
            ReadoutTextSettingsUiState(startLeft, startRight, sustainedLeft, sustainedRight, available)
        }.stateIn(scope, SharingStarted.WhileSubscribed(), ReadoutTextSettingsUiState())

    fun onTextChanged(
        slot: VehicleApproachReadoutTextSlot,
        text: String,
    ) {
        save(slot, text)
    }

    fun onTextReset(slot: VehicleApproachReadoutTextSlot) {
        save(slot, defaultText(slot))
    }

    fun onPreviewClicked(text: String) {
        preview.onPreviewClicked(text, LmuWindowsReadoutItemKey.VehicleApproach.Root)
    }

    fun onPreviewStopped() = preview.stop()

    private fun save(
        slot: VehicleApproachReadoutTextSlot,
        text: String,
    ) {
        when (slot) {
            VehicleApproachReadoutTextSlot.StartLeft -> {
                scope.launch { saveStartLeftText(text) }
            }

            VehicleApproachReadoutTextSlot.StartRight -> {
                scope.launch { saveStartRightText(text) }
            }

            VehicleApproachReadoutTextSlot.SustainedLeft -> {
                scope.launch { vehicleApproachPreferences.saveSustainedLeftReadoutText(text) }
            }

            VehicleApproachReadoutTextSlot.SustainedRight -> {
                scope.launch { vehicleApproachPreferences.saveSustainedRightReadoutText(text) }
            }
        }
    }

    private fun defaultText(slot: VehicleApproachReadoutTextSlot): String =
        when (slot) {
            VehicleApproachReadoutTextSlot.StartLeft -> {
                LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
            }

            VehicleApproachReadoutTextSlot.StartRight -> {
                LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
            }

            VehicleApproachReadoutTextSlot.SustainedLeft -> {
                LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT
            }

            VehicleApproachReadoutTextSlot.SustainedRight -> {
                LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT
            }
        }
}

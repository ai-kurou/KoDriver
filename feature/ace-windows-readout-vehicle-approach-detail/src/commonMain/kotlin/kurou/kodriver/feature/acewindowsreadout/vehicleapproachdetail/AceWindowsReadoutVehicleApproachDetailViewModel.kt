package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachEnabledStateUseCase

/**
 * ACE 車両接近アナウンス詳細設定の ViewModel。
 *
 * 前後・左右の閾値（スライダー）、接近開始時読み上げの有効/無効・読み上げ文言（チップ）はいずれも
 * DataStore に永続化される。チップタップ時はプレビュー再生も行う。
 */
internal class AceWindowsReadoutVehicleApproachDetailViewModel(
    private val thresholds: AceWindowsVehicleApproachThresholdsUseCases,
    private val preferences: AceWindowsVehicleApproachPreferencesUseCases,
    observeEnabledStates: ObserveAceWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveAceWindowsVehicleApproachEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutVehicleApproachDetailUiState> =
        combine(
            thresholds.observeLongitudinalThresholdMeters(),
            thresholds.observeLateralThresholdMeters(),
            observeEnabledStates(),
            preferences.observeStartReadoutType(),
        ) { longitudinal, lateral, enabledStates, startReadoutType ->
            AceWindowsReadoutVehicleApproachDetailUiState(
                longitudinalThresholdMeters = longitudinal,
                lateralThresholdMeters = lateral,
                startReadoutEnabled = enabledStates.getValue(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout),
                startReadoutType = startReadoutType,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutVehicleApproachDetailUiState(),
        )

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLongitudinalThresholdMeters(meters) }
    }

    fun onResetLongitudinalThreshold() {
        viewModelScope.launch {
            thresholds.saveLongitudinalThresholdMeters(
                ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
            )
        }
    }

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLateralThresholdMeters(meters) }
    }

    fun onResetLateralThreshold() {
        viewModelScope.launch {
            thresholds.saveLateralThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
        }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, enabled)
        }
    }

    fun onStartReadoutTypeChanged(type: VehicleApproachStartReadoutType) {
        viewModelScope.launch { preferences.saveStartReadoutType(type) }
        playStartReadoutPreview()
    }

    /**
     * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、周辺車両との直線距離のみが
     * 取得できるため、LMU のような左右を区別した接近アナウンスができない。そのため
     * [VehicleApproachStartReadoutType] の選択に関わらず、常に同じ汎用の接近アナウンス
     * （[SpeechEvent.AceWindowsVehicleApproach]）をプレビュー再生する。
     */
    private fun playStartReadoutPreview() {
        playSpeechEvent(SpeechEvent.AceWindowsVehicleApproach)
    }
}

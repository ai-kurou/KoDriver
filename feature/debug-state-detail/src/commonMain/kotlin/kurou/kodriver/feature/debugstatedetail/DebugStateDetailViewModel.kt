package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

@Suppress("LongParameterList")
internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    observeLmuWindowsTelemetry: ObserveLmuWindowsUseCase,
    observeGt7Ps5Telemetry: ObserveGt7Ps5UseCase,
) : ViewModel() {

    // ドラッグ操作で並び替えたカード順序。永続化はせずインメモリのみで保持する。
    private val _cardOrder = MutableStateFlow(defaultDebugStateCardOrder)

    // LMU / GT7 いずれか片方しか実際には接続されないため、combine の必須ソースにはせず
    // 初期値 null を持つ StateFlow 化して uiState 全体がブロックされないようにする。
    private val _lmuWindowsTelemetry: StateFlow<LmuWindowsTelemetryData?> = observeLmuWindowsTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _gt7Ps5Telemetry: StateFlow<Gt7Ps5TelemetryData?> = observeGt7Ps5Telemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _telemetry: StateFlow<Pair<LmuWindowsTelemetryData?, Gt7Ps5TelemetryData?>> = combine(
        _lmuWindowsTelemetry,
        _gt7Ps5Telemetry,
    ) { lmu, gt7 -> lmu to gt7 }.stateIn(viewModelScope, SharingStarted.Eagerly, null to null)

    val uiState: StateFlow<DebugStateDetailUiState> = combine(
        observeSelectedSimulator(),
        observeRaceFlags(),
        observeVirtualEnergy(),
        _cardOrder,
        _telemetry,
    ) { selectedSimulator, raceFlags, virtualEnergy, cardOrder, (lmuWindowsTelemetry, gt7Ps5Telemetry) ->
        DebugStateDetailUiState(
            selectedSimulator = selectedSimulator,
            raceFlags = raceFlags,
            virtualEnergy = virtualEnergy,
            lmuWindowsTelemetry = lmuWindowsTelemetry,
            gt7Ps5Telemetry = gt7Ps5Telemetry,
            cardOrder = cardOrder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    fun moveCard(fromIndex: Int, toIndex: Int) {
        _cardOrder.update { it.toMutableList().apply { add(toIndex, removeAt(fromIndex)) } }
    }
}

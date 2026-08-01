package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

private data class RaceState(
    val raceFlags: LmuWindowsRaceFlagsData?,
    val virtualEnergy: LmuWindowsVirtualEnergyData?,
    val vehicleApproach: LmuWindowsVehicleApproachData?,
)

private data class OptionalTelemetry(
    val lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    val aceWindowsFuel: AceWindowsFuelData?,
    val aceWindowsFlag: AceWindowsFlagData?,
)

@Suppress("LongParameterList")
internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeLmuWindowsRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeLmuWindowsVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    observeLmuWindowsTelemetry: ObserveLmuWindowsUseCase,
    observeGt7Ps5Telemetry: ObserveGt7Ps5UseCase,
    observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    observeLmuWindowsVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    observeCardOrder: ObserveDebugStateCardOrderUseCase,
    private val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    private val saveCardOrder: SaveDebugStateCardOrderUseCase,
) : ViewModel() {

    private val _selectedSimulator: StateFlow<Simulator?> =
        observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _raceState: StateFlow<RaceState> =
        combine(
        observeLmuWindowsRaceFlags(),
        observeLmuWindowsVirtualEnergy(),
        observeLmuWindowsVehicleApproach(),
    ) { raceFlags, virtualEnergy, vehicleApproach -> RaceState(raceFlags, virtualEnergy, vehicleApproach) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RaceState(null, null, null))

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<DebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<DebugStateCardKey>> =
        combine(
        observeCardOrder(),
        _localCardOrder,
    ) { persisted, local ->
        local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    // LMU / GT7 いずれか片方しか実際には接続されないため、combine の必須ソースにはせず
    // 初期値 null を持つ StateFlow 化して uiState 全体がブロックされないようにする。
    private val _optionalTelemetry: StateFlow<OptionalTelemetry> =
        combine(
        observeLmuWindowsTelemetry().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
        observeGt7Ps5Telemetry().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
        observeAceWindowsFuel().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
        observeAceWindowsFlag().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
    ) { lmu, gt7, aceWindowsFuel, aceWindowsFlag -> OptionalTelemetry(lmu, gt7, aceWindowsFuel, aceWindowsFlag) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, OptionalTelemetry(null, null, null, null))

    val uiState: StateFlow<DebugStateDetailUiState> =
        combine(
        _selectedSimulator,
        _raceState,
        _cardOrder,
        _optionalTelemetry,
    ) { selectedSimulator, raceState, cardOrder, optionalTelemetry ->
        DebugStateDetailUiState(
            selectedSimulator = selectedSimulator,
            raceFlags = raceState.raceFlags,
            virtualEnergy = raceState.virtualEnergy,
            lmuWindowsTelemetry = optionalTelemetry.lmuWindowsTelemetry,
            gt7Ps5Telemetry = optionalTelemetry.gt7Ps5Telemetry,
            aceWindowsFuel = optionalTelemetry.aceWindowsFuel,
            aceWindowsFlag = optionalTelemetry.aceWindowsFlag,
            vehicleApproach = raceState.vehicleApproach,
            cardOrder = cardOrder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val newOrder = _cardOrder.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _localCardOrder.update { newOrder }
        viewModelScope.launch { saveCardOrder(newOrder) }
    }
}

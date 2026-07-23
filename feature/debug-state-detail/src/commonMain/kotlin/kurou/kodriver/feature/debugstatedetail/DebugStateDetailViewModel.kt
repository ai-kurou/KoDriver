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
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

private data class OptionalTelemetry(
    val lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
)

@Suppress("LongParameterList")
internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    observeLmuWindowsTelemetry: ObserveLmuWindowsUseCase,
    observeGt7Ps5Telemetry: ObserveGt7Ps5UseCase,
    observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    observeCardOrder: ObserveDebugStateCardOrderUseCase,
    private val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    private val saveCardOrder: SaveDebugStateCardOrderUseCase,
) : ViewModel() {

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<DebugStateCardKey>?>(null)
    private val _persistedCardOrder: StateFlow<List<DebugStateCardKey>> = observeCardOrder()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _cardOrder: StateFlow<List<DebugStateCardKey>> = combine(
        _persistedCardOrder,
        _localCardOrder,
    ) { persisted, local ->
        local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    // LMU / GT7 いずれか片方しか実際には接続されないため、combine の必須ソースにはせず
    // 初期値 null を持つ StateFlow 化して uiState 全体がブロックされないようにする。
    private val _lmuWindowsTelemetry: StateFlow<LmuWindowsTelemetryData?> = observeLmuWindowsTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _gt7Ps5Telemetry: StateFlow<Gt7Ps5TelemetryData?> = observeGt7Ps5Telemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _optionalTelemetry: StateFlow<OptionalTelemetry> = combine(
        _lmuWindowsTelemetry,
        _gt7Ps5Telemetry,
    ) { lmu, gt7 -> OptionalTelemetry(lmu, gt7) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, OptionalTelemetry(null, null))

    val uiState: StateFlow<DebugStateDetailUiState> = combine(
        observeSelectedSimulator(),
        combine(
            observeRaceFlags(),
            observeVirtualEnergy(),
            observeVehicleApproach(),
        ) { raceFlags, virtualEnergy, vehicleApproach ->
            Triple(raceFlags, virtualEnergy, vehicleApproach)
        },
        _cardOrder,
        _optionalTelemetry,
    ) { selectedSimulator, (raceFlags, virtualEnergy, vehicleApproach), cardOrder, optionalTelemetry ->
        DebugStateDetailUiState(
            selectedSimulator = selectedSimulator,
            raceFlags = raceFlags,
            virtualEnergy = virtualEnergy,
            lmuWindowsTelemetry = optionalTelemetry.lmuWindowsTelemetry,
            gt7Ps5Telemetry = optionalTelemetry.gt7Ps5Telemetry,
            vehicleApproach = vehicleApproach,
            cardOrder = cardOrder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    fun moveCard(fromIndex: Int, toIndex: Int) {
        val newOrder = _cardOrder.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _localCardOrder.update { newOrder }
        viewModelScope.launch { saveCardOrder(newOrder) }
    }
}

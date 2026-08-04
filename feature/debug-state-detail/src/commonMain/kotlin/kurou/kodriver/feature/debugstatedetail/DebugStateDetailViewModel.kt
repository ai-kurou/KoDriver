package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

private data class RaceState(
    val raceFlags: LmuWindowsRaceFlagsData?,
    val virtualEnergy: LmuWindowsVirtualEnergyData?,
    val vehicleApproach: LmuWindowsVehicleApproachData?,
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData?,
    val lmuWindowsVehicleClass: LmuWindowsVehicleClassData?,
)

private data class OptionalTelemetry(
    val lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    val aceWindowsFuel: AceWindowsFuelData?,
    val aceWindowsFlag: AceWindowsFlagData?,
    val gt7Ps5VehicleClass: Gt7Ps5VehicleClassData?,
)

private val lmuWindowsSupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.GAME_PHASE,
        DebugStateCardKey.SESSION,
        DebugStateCardKey.YELLOW_FLAG_STATE,
        DebugStateCardKey.CURRENT_LAP,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.TYRE_TEMPERATURE,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        DebugStateCardKey.TYRE_WEAR,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
    )

private val gt7Ps5SupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.CURRENT_LAP,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.FUEL_CONSUMPTION,
    )

private val aceWindowsSupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.FUEL_CONSUMPTION,
    )

private fun supportedCardKeys(simulator: Simulator?): Set<DebugStateCardKey> =
    when (simulator) {
        is Simulator.LmuWindows -> lmuWindowsSupportedCardKeys
        is Simulator.Gt7Ps5 -> gt7Ps5SupportedCardKeys
        is Simulator.AceWindows -> aceWindowsSupportedCardKeys
        null -> emptySet()
    }

@Suppress("LongParameterList")
internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeLmuWindowsRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeLmuWindowsVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    observeLmuWindowsTelemetry: ObserveLmuWindowsUseCase,
    observeGt7Ps5Telemetry: ObserveGt7Ps5UseCase,
    observeGt7Ps5VehicleClass: ObserveGt7Ps5VehicleClassUseCase,
    observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    observeLmuWindowsVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    observeLmuWindowsTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    observeLmuWindowsVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    observeCardOrder: ObserveDebugStateCardOrderUseCase,
    private val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    private val saveCardOrder: SaveDebugStateCardOrderUseCase,
) : ViewModel() {
    private val _receivedCardKeys = MutableStateFlow<Set<DebugStateCardKey>>(emptySet())

    private val _selectedSimulator: StateFlow<Simulator?> =
        observeSelectedSimulator()
            .onEach { simulator ->
                if (simulator != null) {
                    markCardsReceived(DebugStateCardKey.SIMULATOR)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _raceState: StateFlow<RaceState> =
        combine(
            observeLmuWindowsRaceFlags()
                .onEach {
                    markCardsReceived(
                        DebugStateCardKey.FLAG_INFO,
                        DebugStateCardKey.GAME_PHASE,
                        DebugStateCardKey.YELLOW_FLAG_STATE,
                    )
                },
            observeLmuWindowsVirtualEnergy()
                .onEach {
                    markCardsReceived(
                        DebugStateCardKey.SESSION,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    )
                },
            observeLmuWindowsVehicleApproach()
                .onEach {
                    markCardsReceived(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES)
                },
            observeLmuWindowsTyreCarcassTemperature()
                .onEach {
                    markCardsReceived(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE)
                },
            observeLmuWindowsVehicleClass()
                .onEach {
                    markCardsReceived(DebugStateCardKey.VEHICLE_CLASS)
                },
        ) { raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, lmuWindowsVehicleClass ->
            RaceState(raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, lmuWindowsVehicleClass)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, RaceState(null, null, null, null, null))

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
            observeLmuWindowsTelemetry()
                .onEach {
                    markCardsReceived(
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.TYRE_TEMPERATURE,
                        DebugStateCardKey.TYRE_WEAR,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            observeGt7Ps5Telemetry()
                .onEach {
                    markCardsReceived(
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            observeAceWindowsFuel()
                .onEach {
                    markCardsReceived(DebugStateCardKey.FUEL_CONSUMPTION)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            observeAceWindowsFlag()
                .onEach {
                    markCardsReceived(DebugStateCardKey.FLAG_INFO)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            observeGt7Ps5VehicleClass()
                .onEach {
                    markCardsReceived(DebugStateCardKey.VEHICLE_CLASS)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
        ) { lmu, gt7, aceWindowsFuel, aceWindowsFlag, gt7Ps5VehicleClass ->
            OptionalTelemetry(lmu, gt7, aceWindowsFuel, aceWindowsFlag, gt7Ps5VehicleClass)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, OptionalTelemetry(null, null, null, null, null))

    val uiState: StateFlow<DebugStateDetailUiState> =
        combine(
            _selectedSimulator,
            _raceState,
            _cardOrder,
            _optionalTelemetry,
            _receivedCardKeys,
        ) { selectedSimulator, raceState, cardOrder, optionalTelemetry, receivedCardKeys ->
            DebugStateDetailUiState(
                selectedSimulator = selectedSimulator,
                raceFlags = raceState.raceFlags,
                virtualEnergy = raceState.virtualEnergy,
                lmuWindowsTelemetry = optionalTelemetry.lmuWindowsTelemetry,
                gt7Ps5Telemetry = optionalTelemetry.gt7Ps5Telemetry,
                aceWindowsFuel = optionalTelemetry.aceWindowsFuel,
                aceWindowsFlag = optionalTelemetry.aceWindowsFlag,
                vehicleApproach = raceState.vehicleApproach,
                tyreCarcassTemperature = raceState.tyreCarcassTemperature,
                lmuWindowsVehicleClass = raceState.lmuWindowsVehicleClass,
                gt7Ps5VehicleClass = optionalTelemetry.gt7Ps5VehicleClass,
                enabledCardKeys = receivedCardKeys intersect supportedCardKeys(selectedSimulator),
                cardOrder = cardOrder,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    fun moveCard(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val newOrder = _cardOrder.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _localCardOrder.update { newOrder }
        viewModelScope.launch { saveCardOrder(newOrder) }
    }

    private fun markCardsReceived(vararg cardKeys: DebugStateCardKey) {
        _receivedCardKeys.update { receivedCardKeys -> receivedCardKeys + cardKeys }
    }
}

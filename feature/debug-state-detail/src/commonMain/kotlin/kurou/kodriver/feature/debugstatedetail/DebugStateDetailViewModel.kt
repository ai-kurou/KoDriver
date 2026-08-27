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
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
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
    val lmuWindowsPitStatus: LmuWindowsPitStatusData?,
)

private data class OptionalTelemetry(
    val lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    val aceWindowsFuel: AceWindowsFuelData?,
    val aceWindowsFlag: AceWindowsFlagData?,
    val gt7Ps5VehicleClass: Gt7Ps5VehicleClassData?,
    val aceWindowsStatus: AceWindowsStatusData?,
    val aceWindowsTyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData?,
    val aceWindowsVehicleApproach: AceWindowsVehicleApproachData?,
)

private val lmuWindowsSupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.VEHICLE_LOCATION,
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
        DebugStateCardKey.VEHICLE_LOCATION,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
    )

private fun supportedCardKeys(simulator: Simulator): Set<DebugStateCardKey> =
    when (simulator) {
        is Simulator.LmuWindows -> lmuWindowsSupportedCardKeys
        is Simulator.Gt7Ps5 -> gt7Ps5SupportedCardKeys
        is Simulator.AceWindows -> aceWindowsSupportedCardKeys
    }

internal data class LmuWindowsDebugStateUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeTelemetry: ObserveLmuWindowsUseCase,
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observePitStatus: ObserveLmuWindowsPitStatusUseCase,
)

internal data class Gt7Ps5DebugStateUseCases(
    val observeTelemetry: ObserveGt7Ps5UseCase,
    val observeVehicleClass: ObserveGt7Ps5VehicleClassUseCase,
)

internal data class AceWindowsDebugStateUseCases(
    val observeFuel: ObserveAceWindowsFuelUseCase,
    val observeFlag: ObserveAceWindowsFlagUseCase,
    val observeStatus: ObserveAceWindowsStatusUseCase,
    val observeTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeVehicleApproach: ObserveAceWindowsVehicleApproachUseCase,
)

internal data class DebugStateCardOrderUseCases(
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
)

internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    lmuWindowsUseCases: LmuWindowsDebugStateUseCases,
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
    aceWindowsUseCases: AceWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
) : ViewModel() {
    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases.saveCardOrder
    private val _receivedCardKeys = MutableStateFlow<Map<Simulator, Set<DebugStateCardKey>>>(emptyMap())

    private val _selectedSimulator: StateFlow<Simulator> =
        observeSelectedSimulator()
            .onEach { simulator ->
                markCardsReceived(simulator, DebugStateCardKey.SIMULATOR)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    private val _raceStateBase: StateFlow<RaceState> =
        combine(
            lmuWindowsUseCases
                .observeRaceFlags()
                .onEach {
                    markCardsReceived(
                        Simulator.LmuWindows,
                        DebugStateCardKey.FLAG_INFO,
                        DebugStateCardKey.GAME_PHASE,
                        DebugStateCardKey.YELLOW_FLAG_STATE,
                    )
                },
            lmuWindowsUseCases
                .observeVirtualEnergy()
                .onEach {
                    markCardsReceived(
                        Simulator.LmuWindows,
                        DebugStateCardKey.SESSION,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    )
                },
            lmuWindowsUseCases
                .observeVehicleApproach()
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, DebugStateCardKey.SIDE_BY_SIDE_VEHICLES)
                },
            lmuWindowsUseCases
                .observeTyreCarcassTemperature()
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, DebugStateCardKey.TYRE_CARCASS_TEMPERATURE)
                },
            lmuWindowsUseCases
                .observeVehicleClass()
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, DebugStateCardKey.VEHICLE_CLASS)
                },
        ) { raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, lmuWindowsVehicleClass ->
            RaceState(raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, lmuWindowsVehicleClass, null)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, RaceState(null, null, null, null, null, null))

    private val _lmuWindowsPitStatus: StateFlow<LmuWindowsPitStatusData?> =
        lmuWindowsUseCases
            .observePitStatus()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, DebugStateCardKey.VEHICLE_LOCATION)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _raceState: StateFlow<RaceState> =
        combine(_raceStateBase, _lmuWindowsPitStatus) { base, lmuWindowsPitStatus ->
            base.copy(lmuWindowsPitStatus = lmuWindowsPitStatus)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, RaceState(null, null, null, null, null, null))

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<DebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<DebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeCardOrder(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    // LMU / GT7 いずれか片方しか実際には接続されないため、combine の必須ソースにはせず
    // 初期値 null を持つ StateFlow 化して uiState 全体がブロックされないようにする。
    private val _optionalTelemetryBase: StateFlow<OptionalTelemetry> =
        combine(
            lmuWindowsUseCases
                .observeTelemetry()
                .onEach {
                    markCardsReceived(
                        Simulator.LmuWindows,
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.TYRE_TEMPERATURE,
                        DebugStateCardKey.TYRE_WEAR,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            gt7Ps5UseCases
                .observeTelemetry()
                .onEach {
                    markCardsReceived(
                        Simulator.Gt7Ps5,
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            aceWindowsUseCases
                .observeFuel()
                .onEach {
                    markCardsReceived(Simulator.AceWindows, DebugStateCardKey.FUEL_CONSUMPTION)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            aceWindowsUseCases
                .observeFlag()
                .onEach {
                    markCardsReceived(Simulator.AceWindows, DebugStateCardKey.FLAG_INFO)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
            gt7Ps5UseCases
                .observeVehicleClass()
                .onEach {
                    markCardsReceived(Simulator.Gt7Ps5, DebugStateCardKey.VEHICLE_CLASS)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null),
        ) { lmu, gt7, aceWindowsFuel, aceWindowsFlag, gt7Ps5VehicleClass ->
            OptionalTelemetry(lmu, gt7, aceWindowsFuel, aceWindowsFlag, gt7Ps5VehicleClass, null, null, null)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            OptionalTelemetry(null, null, null, null, null, null, null, null),
        )

    private val _aceWindowsStatus: StateFlow<AceWindowsStatusData?> =
        aceWindowsUseCases
            .observeStatus()
            .onEach {
                markCardsReceived(Simulator.AceWindows, DebugStateCardKey.VEHICLE_LOCATION)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _aceWindowsTyreCarcassTemperature: StateFlow<AceWindowsTyreCarcassTemperatureData?> =
        aceWindowsUseCases
            .observeTyreCarcassTemperature()
            .onEach {
                markCardsReceived(Simulator.AceWindows, DebugStateCardKey.TYRE_CARCASS_TEMPERATURE)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _aceWindowsVehicleApproach: StateFlow<AceWindowsVehicleApproachData?> =
        aceWindowsUseCases
            .observeVehicleApproach()
            .onEach {
                markCardsReceived(Simulator.AceWindows, DebugStateCardKey.SIDE_BY_SIDE_VEHICLES)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _optionalTelemetry: StateFlow<OptionalTelemetry> =
        combine(
            _optionalTelemetryBase,
            _aceWindowsStatus,
            _aceWindowsTyreCarcassTemperature,
            _aceWindowsVehicleApproach,
        ) { base, aceWindowsStatus, aceWindowsTyreCarcassTemperature, aceWindowsVehicleApproach ->
            base.copy(
                aceWindowsStatus = aceWindowsStatus,
                aceWindowsTyreCarcassTemperature = aceWindowsTyreCarcassTemperature,
                aceWindowsVehicleApproach = aceWindowsVehicleApproach,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            OptionalTelemetry(null, null, null, null, null, null, null, null),
        )

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
                aceWindowsStatus = optionalTelemetry.aceWindowsStatus,
                lmuWindowsPitStatus = raceState.lmuWindowsPitStatus,
                vehicleApproach = raceState.vehicleApproach,
                aceWindowsVehicleApproach = optionalTelemetry.aceWindowsVehicleApproach,
                tyreCarcassTemperature = raceState.tyreCarcassTemperature,
                aceWindowsTyreCarcassTemperature = optionalTelemetry.aceWindowsTyreCarcassTemperature,
                lmuWindowsVehicleClass = raceState.lmuWindowsVehicleClass,
                gt7Ps5VehicleClass = optionalTelemetry.gt7Ps5VehicleClass,
                enabledCardKeys =
                    receivedCardKeys[selectedSimulator].orEmpty() intersect supportedCardKeys(selectedSimulator),
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

    private fun markCardsReceived(
        simulator: Simulator,
        vararg cardKeys: DebugStateCardKey,
    ) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + (simulator to (receivedCardKeys[simulator].orEmpty() + cardKeys))
        }
    }
}

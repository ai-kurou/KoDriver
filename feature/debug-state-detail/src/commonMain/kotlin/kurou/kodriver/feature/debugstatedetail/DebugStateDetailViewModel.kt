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
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import kotlin.time.Clock

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
        DebugStateCardKey.BRAKE_TEMPERATURE,
        DebugStateCardKey.BRAKE_WEAR,
        DebugStateCardKey.TYRE_WEAR,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
        DebugStateCardKey.VEHICLE_DAMAGE,
    )

private val gt7Ps5SupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.CURRENT_LAP,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.TYRE_TEMPERATURE,
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
        DebugStateCardKey.BEST_LAP,
    )

private fun supportedCardKeys(simulator: Simulator): Set<DebugStateCardKey> =
    when (simulator) {
        is Simulator.LmuWindows -> lmuWindowsSupportedCardKeys
        is Simulator.Gt7Ps5 -> gt7Ps5SupportedCardKeys
        is Simulator.AceWindows -> aceWindowsSupportedCardKeys
    }

internal data class DebugStateCardOrderUseCases(
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
)

private const val SIDE_BY_SIDE_TICK_INTERVAL_MS = 100L

internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    lmuWindowsUseCases: LmuWindowsDebugStateUseCases,
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
    aceWindowsUseCases: AceWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val sideBySideTickIntervalMs: Long = SIDE_BY_SIDE_TICK_INTERVAL_MS,
) : ViewModel() {
    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases.saveCardOrder
    private val _receivedCardKeys = MutableStateFlow<Map<Simulator, Set<DebugStateCardKey>>>(emptyMap())

    private val _selectedSimulator: StateFlow<Simulator> =
        observeSelectedSimulator()
            .onEach { simulator ->
                markCardsReceived(simulator, setOf(DebugStateCardKey.SIMULATOR))
            }.stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    private val lmuWindowsSource =
        LmuWindowsDebugStateSource(
            scope = viewModelScope,
            useCases = lmuWindowsUseCases,
            markCardsReceived = ::markCardsReceived,
            currentTimeMs = currentTimeMs,
            sideBySideTickIntervalMs = sideBySideTickIntervalMs,
        )
    private val gt7Ps5Source =
        Gt7Ps5DebugStateSource(viewModelScope, gt7Ps5UseCases, ::markCardsReceived)
    private val aceWindowsSource =
        AceWindowsDebugStateSource(viewModelScope, aceWindowsUseCases, ::markCardsReceived)

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<DebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<DebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeCardOrder(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val _header: StateFlow<DebugStateDetailUiState> =
        combine(
            _selectedSimulator,
            _cardOrder,
            _receivedCardKeys,
        ) { selectedSimulator, cardOrder, receivedCardKeys ->
            DebugStateDetailUiState(
                selectedSimulator = selectedSimulator,
                enabledCardKeys =
                    receivedCardKeys[selectedSimulator].orEmpty() intersect supportedCardKeys(selectedSimulator),
                cardOrder = cardOrder,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    val uiState: StateFlow<DebugStateDetailUiState> =
        combine(_header, lmuWindowsSource.state, gt7Ps5Source.state, aceWindowsSource.state) { header, lmu, gt7, ace ->
            header.copy(lmuWindows = lmu, gt7Ps5 = gt7, aceWindows = ace)
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
        cardKeys: Set<DebugStateCardKey>,
    ) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + (simulator to (receivedCardKeys[simulator].orEmpty() + cardKeys))
        }
    }
}

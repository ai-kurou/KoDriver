package kurou.kodriver.feature.readoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

private data class LocalOrderState(
    val simulator: Simulator?,
    val items: List<ReadoutItemKey>,
)

private data class EnabledStates(
    val readoutEnabledStates: Map<ReadoutItemKey, Boolean>,
    val queueEnabledStates: Map<ReadoutItemKey, Boolean>,
    val startSoundEnabledStates: Map<ReadoutItemKey, Boolean>,
)

data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
)

data class ReadoutOrderUseCases(
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
    val resolveReadoutOrder: ResolveReadoutOrderUseCase,
    val saveReadoutOrder: SaveReadoutOrderUseCase,
)

data class ReadoutEnabledUseCases(
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

data class QueueUseCases(
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
    val saveQueueEnabledState: SaveQueueEnabledStateUseCase,
)

data class StartSoundUseCases(
    val observeReadoutStartSoundEnabledStates: ObserveReadoutStartSoundEnabledStatesUseCase,
    val saveReadoutStartSoundEnabledState: SaveReadoutStartSoundEnabledStateUseCase,
)

/**
 * ReadoutList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class ReadoutListViewModel(
    private val simulatorUseCases: SimulatorUseCases,
    private val readoutOrderUseCases: ReadoutOrderUseCases,
    private val readoutEnabledUseCases: ReadoutEnabledUseCases,
    private val queueUseCases: QueueUseCases,
    private val startSoundUseCases: StartSoundUseCases,
) : ViewModel() {
    private val _selectedSimulator: StateFlow<Simulator?> =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ドラッグ操作後のインメモリ順序（DataStore 反映前の即時 UI 更新用）
    private val _localOrder = MutableStateFlow(LocalOrderState(null, emptyList()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _persistedOrder: StateFlow<List<ReadoutItemKey>> =
        _selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator != null) {
                    readoutOrderUseCases.observeReadoutOrder(simulator.id)
                } else {
                    flowOf(emptyList())
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _readoutEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        _selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator != null) {
                    readoutEnabledUseCases.observeReadoutEnabledStates(simulator.id)
                } else {
                    flowOf(emptyMap())
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _queueEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        queueUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _startSoundEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        startSoundUseCases
            .observeReadoutStartSoundEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _selectedItem = MutableStateFlow<ReadoutListItemType?>(null)

    private val _effectiveOrder: StateFlow<List<ReadoutItemKey>> =
        combine(
            _selectedSimulator,
            _persistedOrder,
            _localOrder,
        ) { selected, persisted, local ->
            val defaultItems = selected?.let { ReadoutListItemType.defaultOrder(it) }.orEmpty()
            // ドラッグ中の localOrder を最優先（DataStore の非同期更新より常に新しい）
            if (local.simulator == selected) {
                local.items
            } else {
                readoutOrderUseCases.resolveReadoutOrder(persistedOrder = persisted, defaultOrder = defaultItems)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _enabledStates: StateFlow<EnabledStates> =
        combine(
            _readoutEnabledStates,
            _queueEnabledStates,
            _startSoundEnabledStates,
        ) { readoutEnabledStates, queueEnabledStates, startSoundEnabledStates ->
            EnabledStates(readoutEnabledStates, queueEnabledStates, startSoundEnabledStates)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            EnabledStates(emptyMap(), emptyMap(), emptyMap()),
        )

    val uiState: StateFlow<ReadoutListUiState> =
        combine(
            _selectedSimulator,
            _effectiveOrder,
            _enabledStates,
            _selectedItem,
        ) { selected, items, enabledStates, selectedItem ->
            ReadoutListUiState(
                selectedSimulator = selected,
                simulators = Simulator.entries,
                items = items,
                readoutEnabledStates = enabledStates.readoutEnabledStates,
                queueEnabledStates = enabledStates.queueEnabledStates,
                startSoundEnabledStates = enabledStates.startSoundEnabledStates,
                selectedItem = selectedItem?.takeIf { selected != null && it.belongsTo(selected) },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ReadoutListUiState(simulators = Simulator.entries),
        )

    fun onSimulatorSelected(simulator: Simulator) {
        viewModelScope.launch {
            simulatorUseCases.saveSelectedSimulator(simulator)
        }
    }

    fun moveItem(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val selected = _selectedSimulator.value ?: return
        val newItems =
            _effectiveOrder.value
                .toMutableList()
                .also { it.add(toIndex, it.removeAt(fromIndex)) }
        _localOrder.update { LocalOrderState(selected, newItems) }
        viewModelScope.launch {
            readoutOrderUseCases.saveReadoutOrder(selected.id, newItems)
        }
    }

    fun onItemSelected(item: ReadoutItemKey) {
        val simulator = _selectedSimulator.value ?: return
        val type = ReadoutListItemType.fromId(simulator, item) ?: return
        _selectedItem.update { if (it == type) null else type }
    }

    fun clearSelectedItem() {
        _selectedItem.update { null }
    }

    fun onReadoutEnabledChanged(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        val simulator = _selectedSimulator.value ?: return
        viewModelScope.launch {
            readoutEnabledUseCases.saveReadoutEnabledState(simulator.id, key, enabled)
        }
    }

    fun onQueueEnabledChanged(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            queueUseCases.saveQueueEnabledState(key, enabled)
        }
    }

    fun onStartSoundEnabledChanged(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            startSoundUseCases.saveReadoutStartSoundEnabledState(key, enabled)
        }
    }
}

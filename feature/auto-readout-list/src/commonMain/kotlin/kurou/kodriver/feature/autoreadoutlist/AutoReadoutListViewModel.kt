package kurou.kodriver.feature.autoreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundEnabledStateUseCase

private data class LocalOrderState(
    val simulator: Simulator?,
    val items: List<ReadoutItemKey>,
)

private data class EnabledStates(
    val autoReadoutEnabledStates: Map<ReadoutItemKey, Boolean>,
    val queueEnabledStates: Map<ReadoutItemKey, Boolean>,
    val startSoundEnabledStates: Map<ReadoutItemKey, Boolean>,
)

data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
)

data class AutoReadoutOrderUseCases(
    val observeAutoReadoutOrder: ObserveAutoReadoutOrderUseCase,
    val resolveAutoReadoutOrder: ResolveAutoReadoutOrderUseCase,
    val saveAutoReadoutOrder: SaveAutoReadoutOrderUseCase,
)

data class AutoReadoutEnabledUseCases(
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
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
 * AutoReadoutList 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class AutoReadoutListViewModel(
    private val simulatorUseCases: SimulatorUseCases,
    private val autoReadoutOrderUseCases: AutoReadoutOrderUseCases,
    private val autoReadoutEnabledUseCases: AutoReadoutEnabledUseCases,
    private val queueUseCases: QueueUseCases,
    private val startSoundUseCases: StartSoundUseCases,
) : ViewModel() {
    private val _selectedSimulator: StateFlow<Simulator> =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    // ドラッグ操作後のインメモリ順序（DataStore 反映前の即時 UI 更新用）
    private val _localOrder = MutableStateFlow(LocalOrderState(null, emptyList()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _persistedOrder: StateFlow<List<ReadoutItemKey>> =
        _selectedSimulator
            .flatMapLatest { simulator ->
                autoReadoutOrderUseCases.observeAutoReadoutOrder(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _autoReadoutEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        _selectedSimulator
            .flatMapLatest { simulator ->
                autoReadoutEnabledUseCases.observeAutoReadoutEnabledStates(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _queueEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        queueUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _startSoundEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> =
        startSoundUseCases
            .observeReadoutStartSoundEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _selectedItem = MutableStateFlow<AutoReadoutListItemType?>(null)

    private val _effectiveOrder: StateFlow<List<ReadoutItemKey>> =
        combine(
            _selectedSimulator,
            _persistedOrder,
            _localOrder,
        ) { selected, persisted, local ->
            val defaultItems = AutoReadoutListItemType.defaultOrder(selected)
            // ドラッグ中の localOrder を最優先（DataStore の非同期更新より常に新しい）
            if (local.simulator == selected) {
                local.items
            } else {
                autoReadoutOrderUseCases.resolveAutoReadoutOrder(
                    persistedOrder = persisted,
                    defaultOrder = defaultItems,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _enabledStates: StateFlow<EnabledStates> =
        combine(
            _autoReadoutEnabledStates,
            _queueEnabledStates,
            _startSoundEnabledStates,
        ) { autoReadoutEnabledStates, queueEnabledStates, startSoundEnabledStates ->
            EnabledStates(autoReadoutEnabledStates, queueEnabledStates, startSoundEnabledStates)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            EnabledStates(emptyMap(), emptyMap(), emptyMap()),
        )

    val uiState: StateFlow<AutoReadoutListUiState> =
        combine(
            _selectedSimulator,
            _effectiveOrder,
            _enabledStates,
            _selectedItem,
        ) { selected, items, enabledStates, selectedItem ->
            AutoReadoutListUiState(
                selectedSimulator = selected,
                items = items,
                autoReadoutEnabledStates = enabledStates.autoReadoutEnabledStates,
                queueEnabledStates = enabledStates.queueEnabledStates,
                startSoundEnabledStates = enabledStates.startSoundEnabledStates,
                selectedItem = selectedItem?.takeIf { it.belongsTo(selected) },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AutoReadoutListUiState(),
        )

    fun moveItem(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val selected = _selectedSimulator.value
        val newItems =
            _effectiveOrder.value
                .toMutableList()
                .also { it.add(toIndex, it.removeAt(fromIndex)) }
        _localOrder.update { LocalOrderState(selected, newItems) }
        viewModelScope.launch {
            autoReadoutOrderUseCases.saveAutoReadoutOrder(selected.id, newItems)
        }
    }

    fun onItemSelected(item: ReadoutItemKey) {
        val simulator = _selectedSimulator.value
        val type = AutoReadoutListItemType.fromId(simulator, item) ?: return
        _selectedItem.update { if (it == type) null else type }
    }

    fun clearSelectedItem() {
        _selectedItem.update { null }
    }

    fun onReadoutEnabledChanged(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        val simulator = _selectedSimulator.value
        viewModelScope.launch {
            autoReadoutEnabledUseCases.saveAutoReadoutEnabledState(simulator.id, key, enabled)
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

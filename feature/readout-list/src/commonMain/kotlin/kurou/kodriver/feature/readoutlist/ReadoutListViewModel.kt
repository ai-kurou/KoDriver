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
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

private val simulatorItems: Map<String, List<ReadoutItemKey>> = mapOf(
    "lmu_windows" to listOf(
        ReadoutItemKey.FLAG,
        ReadoutItemKey.VEHICLE_APPROACH,
        ReadoutItemKey.VEHICLE_DAMAGE,
    ),
    "gt7_ps5" to listOf(
        ReadoutItemKey.BEST_LAP,
    ),
)

private val simulators: List<String> = simulatorItems.keys.toList()

private data class LocalOrderState(
    val simulator: String?,
    val items: List<ReadoutItemKey>,
)

internal class ReadoutListViewModel(
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
    private val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val observeReadoutOrder: ObserveReadoutOrderUseCase,
    private val saveReadoutOrder: SaveReadoutOrderUseCase,
) : ViewModel() {

    private val _selectedSimulator: StateFlow<String?> = observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // ドラッグ操作後のインメモリ順序（DataStore 反映前の即時 UI 更新用）
    private val _localOrder = MutableStateFlow(LocalOrderState(null, emptyList()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _persistedOrder: StateFlow<List<ReadoutItemKey>> = _selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != null) observeReadoutOrder(simulator)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _readoutEnabledStates: StateFlow<Map<ReadoutItemKey, Boolean>> = _selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != null) observeReadoutEnabledStates(simulator)
            else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _selectedItem = MutableStateFlow<ReadoutListItemType?>(null)

    private val _effectiveOrder: StateFlow<List<ReadoutItemKey>> = combine(
        _selectedSimulator,
        _persistedOrder,
        _localOrder,
    ) { selected, persisted, local ->
        val defaultItems = simulatorItems[selected].orEmpty()
        when {
            // ドラッグ中の localOrder を最優先（DataStore の非同期更新より常に新しい）
            local.simulator == selected -> local.items
            persisted.isNotEmpty() -> {
                val ordered = persisted.filter { it in defaultItems }
                val unordered = defaultItems.filter { it !in persisted }
                ordered + unordered
            }
            else -> defaultItems
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState: StateFlow<ReadoutListUiState> = combine(
        _selectedSimulator,
        _effectiveOrder,
        _readoutEnabledStates,
        _selectedItem,
    ) { selected, items, readoutEnabledStates, selectedItem ->
        ReadoutListUiState(
            selectedSimulator = selected,
            simulators = simulators,
            items = items,
            readoutEnabledStates = readoutEnabledStates,
            selectedItem = selectedItem,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReadoutListUiState(simulators = simulators),
    )

    fun onSimulatorSelected(simulator: String) {
        viewModelScope.launch {
            saveSelectedSimulator(simulator)
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val selected = _selectedSimulator.value ?: return
        val newItems = _effectiveOrder.value.toMutableList()
            .also { it.add(toIndex, it.removeAt(fromIndex)) }
        _localOrder.update { LocalOrderState(selected, newItems) }
        viewModelScope.launch {
            saveReadoutOrder(selected, newItems)
        }
    }

    fun onItemSelected(item: ReadoutItemKey) {
        val type = ReadoutListItemType.fromId(item) ?: return
        _selectedItem.update { if (it == type) null else type }
    }

    fun clearSelectedItem() {
        _selectedItem.update { null }
    }

    fun onReadoutEnabledChanged(key: ReadoutItemKey, enabled: Boolean) {
        val simulator = _selectedSimulator.value ?: return
        viewModelScope.launch {
            saveReadoutEnabledState(simulator, key, enabled)
        }
    }
}

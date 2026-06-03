package kurou.kodriver.feature.readout

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
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

private val simulatorItems: Map<String, List<String>> = mapOf(
    "lmu" to listOf("vehicle_approach", "laps_remaining"),
)

private val simulators: List<String> = simulatorItems.keys.toList()

private data class ItemsState(
    val simulator: String?,
    val items: List<String>,
)

class ReadoutViewModel(
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
    private val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
) : ViewModel() {

    private val _selectedSimulator: StateFlow<String?> = observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _itemsState = MutableStateFlow(ItemsState(null, emptyList()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _readoutEnabledStates: StateFlow<Map<String, Boolean>> = _selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != null) observeReadoutEnabledStates(simulator)
            else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val uiState: StateFlow<ReadoutListUiState> = combine(
        _selectedSimulator,
        _itemsState,
        _readoutEnabledStates,
    ) { selected, itemsState, readoutEnabledStates ->
        val items = if (itemsState.simulator == selected) itemsState.items
        else simulatorItems[selected].orEmpty()
        ReadoutListUiState(
            selectedSimulator = selected,
            simulators = simulators,
            items = items,
            readoutEnabledStates = readoutEnabledStates,
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

    fun moveItemUp(index: Int) {
        if (index <= 0) return
        _itemsState.update { state ->
            val current = effectiveItemsStateFrom(state)
            current.copy(items = current.items.toMutableList().also { it.add(index - 1, it.removeAt(index)) })
        }
    }

    fun moveItemDown(index: Int) {
        _itemsState.update { state ->
            val current = effectiveItemsStateFrom(state)
            if (index >= current.items.lastIndex) return@update state
            current.copy(items = current.items.toMutableList().also { it.add(index + 1, it.removeAt(index)) })
        }
    }

    fun onReadoutEnabledChanged(label: String, enabled: Boolean) {
        val simulator = _selectedSimulator.value ?: return
        viewModelScope.launch {
            saveReadoutEnabledState(simulator, label, enabled)
        }
    }

    private fun effectiveItemsStateFrom(state: ItemsState): ItemsState {
        val selected = _selectedSimulator.value
        return if (state.simulator == selected) state
        else ItemsState(selected, simulatorItems[selected].orEmpty())
    }
}

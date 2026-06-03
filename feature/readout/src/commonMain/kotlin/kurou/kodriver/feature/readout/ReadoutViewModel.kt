package kurou.kodriver.feature.readout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase

private val simulatorItems: Map<String, List<String>> = mapOf(
    "Le Mans Ultimate" to listOf("車両接近", "残りラップ数"),
)

private val simulators: List<String> = simulatorItems.keys.toList()

private data class ItemsState(
    val simulator: String?,
    val items: List<String>,
    val readoutEnabledStates: Map<String, Boolean>,
)

class ReadoutViewModel(
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val saveSelectedSimulator: SaveSelectedSimulatorUseCase,
) : ViewModel() {

    private val _selectedSimulator: StateFlow<String?> = observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _itemsState = MutableStateFlow(ItemsState(null, emptyList(), emptyMap()))

    val uiState: StateFlow<ReadoutListUiState> = combine(
        _selectedSimulator,
        _itemsState,
    ) { selected, itemsState ->
        val (items, readoutEnabledStates) = if (itemsState.simulator == selected) {
            itemsState.items to itemsState.readoutEnabledStates
        } else {
            simulatorItems[selected].orEmpty() to emptyMap()
        }
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
        _itemsState.update { state ->
            effectiveItemsStateFrom(state).let { current ->
                current.copy(readoutEnabledStates = current.readoutEnabledStates + (label to enabled))
            }
        }
    }

    private fun effectiveItemsStateFrom(state: ItemsState): ItemsState {
        val selected = _selectedSimulator.value
        return if (state.simulator == selected) state
        else ItemsState(selected, simulatorItems[selected].orEmpty(), emptyMap())
    }
}

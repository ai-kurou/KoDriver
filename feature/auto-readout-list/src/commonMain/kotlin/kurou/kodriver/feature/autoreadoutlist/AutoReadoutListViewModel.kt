package kurou.kodriver.feature.autoreadoutlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
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
    private val _selectedSimulator =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    // ドラッグ操作後のインメモリ順序（DataStore 反映前の即時 UI 更新用）
    private val _localOrder = MutableStateFlow(LocalOrderState(null, emptyList()))

    private val _selectedItem = MutableStateFlow<AutoReadoutListItemType?>(null)
    private val flow =
        AutoReadoutListFlow(
            observeSelectedSimulator = _selectedSimulator,
            autoReadoutOrderUseCases = autoReadoutOrderUseCases,
            autoReadoutEnabledUseCases = autoReadoutEnabledUseCases,
            queueUseCases = queueUseCases,
            startSoundUseCases = startSoundUseCases,
            localOrder = _localOrder.map { it.simulator to it.items },
            selectedItem = _selectedItem,
        )

    val uiState =
        flow.uiState.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AutoReadoutListUiState(),
        )

    fun moveItem(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val selected = _selectedSimulator.value
        val newItems =
            uiState.value.items
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

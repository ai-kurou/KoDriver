package kurou.kodriver.feature.debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal class DebugStateDetailViewModel(
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
) : ViewModel() {

    // ドラッグ操作で並び替えたカード順序。永続化はせずインメモリのみで保持する。
    private val _cardOrder = MutableStateFlow(defaultDebugStateCardOrder)

    val uiState: StateFlow<DebugStateDetailUiState> = combine(
        observeSelectedSimulator(),
        observeRaceFlags(),
        observeVirtualEnergy(),
        _cardOrder,
    ) { selectedSimulator, raceFlags, virtualEnergy, cardOrder ->
        DebugStateDetailUiState(
            selectedSimulator = selectedSimulator,
            raceFlags = raceFlags,
            virtualEnergy = virtualEnergy,
            cardOrder = cardOrder,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugStateDetailUiState())

    fun moveCard(fromIndex: Int, toIndex: Int) {
        _cardOrder.update { it.toMutableList().apply { add(toIndex, removeAt(fromIndex)) } }
    }
}

package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

internal data class DebugStateCardOrderUseCases(
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
)

internal class AceWindowsDebugStateDetailViewModel(
    aceWindowsUseCases: AceWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
) : ViewModel() {
    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases.saveCardOrder
    private val _receivedCardKeys = MutableStateFlow(setOf(DebugStateCardKey.SIMULATOR))

    private val aceWindowsSource =
        AceWindowsDebugStateSource(
            scope = viewModelScope,
            useCases = aceWindowsUseCases,
            markCardsReceived = ::markCardsReceived,
        )

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<DebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<DebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeCardOrder(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val _header: StateFlow<AceWindowsDebugStateDetailUiState> =
        combine(
            _cardOrder,
            _receivedCardKeys,
        ) { cardOrder, receivedCardKeys ->
            AceWindowsDebugStateDetailUiState(
                enabledCardKeys = receivedCardKeys intersect aceWindowsSupportedCardKeys,
                cardOrder = cardOrder,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AceWindowsDebugStateDetailUiState())

    val uiState: StateFlow<AceWindowsDebugStateDetailUiState> =
        combine(_header, aceWindowsSource.state) { header, ace ->
            header.copy(aceWindows = ace)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AceWindowsDebugStateDetailUiState())

    fun moveCard(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val newOrder = _cardOrder.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _localCardOrder.update { newOrder }
        viewModelScope.launch { saveCardOrder(newOrder) }
    }

    private fun markCardsReceived(cardKeys: Set<DebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + cardKeys
        }
    }
}

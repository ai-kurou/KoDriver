package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class Gt7Ps5DebugStateDetailViewModel(
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
) : ViewModel() {
    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases::saveKeys
    private val _receivedCardKeys = MutableStateFlow(setOf(Gt7Ps5DebugStateCardKey.SIMULATOR))

    private val gt7Ps5Source =
        Gt7Ps5DebugStateSource(
            scope = viewModelScope,
            useCases = gt7Ps5UseCases,
            markCardsReceived = ::markCardsReceived,
        )

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<Gt7Ps5DebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<Gt7Ps5DebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeKeys(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val _header: StateFlow<Gt7Ps5DebugStateDetailUiState> =
        combine(
            _cardOrder,
            _receivedCardKeys,
        ) { cardOrder, receivedCardKeys ->
            Gt7Ps5DebugStateDetailUiState(
                enabledCardKeys = receivedCardKeys intersect gt7Ps5SupportedCardKeys,
                cardOrder = cardOrder,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Gt7Ps5DebugStateDetailUiState())

    val uiState: StateFlow<Gt7Ps5DebugStateDetailUiState> =
        combine(_header, gt7Ps5Source.state) { header, gt7Ps5 ->
            header.copy(gt7Ps5 = gt7Ps5)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Gt7Ps5DebugStateDetailUiState())

    fun moveCard(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val newOrder = _cardOrder.value.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        _localCardOrder.update { newOrder }
        viewModelScope.launch { saveCardOrder(newOrder) }
    }

    private fun markCardsReceived(cardKeys: Set<Gt7Ps5DebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + cardKeys
        }
    }
}

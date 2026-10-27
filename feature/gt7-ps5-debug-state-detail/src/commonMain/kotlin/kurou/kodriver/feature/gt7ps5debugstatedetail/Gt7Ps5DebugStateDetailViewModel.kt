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
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.GT7_PS5_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase

internal class Gt7Ps5DebugStateDetailViewModel(
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val requestDebugReadout: RequestDebugReadoutUseCase,
) : ViewModel() {
    private val _selectedReadoutItemKey =
        MutableStateFlow<ReadoutItemKey>(
            GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().first(),
        )
    private val _debugReadoutText = MutableStateFlow("")

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

    private val debugStateDetailFlow =
        Gt7Ps5DebugStateDetailFlow(
            cardOrder = _cardOrder,
            receivedCardKeys = _receivedCardKeys,
            gt7Ps5 = gt7Ps5Source.state,
            selectedReadoutItemKey = _selectedReadoutItemKey,
            debugReadoutText = _debugReadoutText,
        )

    private val _header: StateFlow<Gt7Ps5DebugStateDetailUiState> =
        debugStateDetailFlow.header.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5DebugStateDetailUiState(),
        )

    val uiState: StateFlow<Gt7Ps5DebugStateDetailUiState> =
        debugStateDetailFlow.detail(_header).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5DebugStateDetailUiState(),
        )

    fun selectReadoutItem(value: String) {
        val key =
            GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().find { it.value == value } ?: return
        _selectedReadoutItemKey.update { key }
    }

    fun updateDebugReadoutText(text: String) {
        _debugReadoutText.update { text }
    }

    fun requestReadout() {
        val text = _debugReadoutText.value
        if (text.isBlank()) return
        val request = DebugReadoutRequest(Simulator.Gt7Ps5, _selectedReadoutItemKey.value, text)
        viewModelScope.launch { requestDebugReadout(request) }
    }

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

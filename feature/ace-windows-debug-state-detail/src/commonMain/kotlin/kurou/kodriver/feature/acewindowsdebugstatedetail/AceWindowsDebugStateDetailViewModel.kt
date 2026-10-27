package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ACE_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase

internal data class DebugStateCardOrderUseCases(
    val observeCardOrder: ObserveDebugStateCardOrderUseCase,
    val resolveCardOrder: ResolveDebugStateCardOrderUseCase,
    val saveCardOrder: SaveDebugStateCardOrderUseCase,
) {
    fun observeKeys(): Flow<List<AceWindowsDebugStateCardKey>> =
        observeCardOrder().map { order ->
            order.mapNotNull { name -> AceWindowsDebugStateCardKey.entries.find { it.name == name } }
        }

    suspend fun saveKeys(order: List<AceWindowsDebugStateCardKey>) = saveCardOrder(order.map { it.name })
}

internal class AceWindowsDebugStateDetailViewModel(
    aceWindowsUseCases: AceWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val requestDebugReadout: RequestDebugReadoutUseCase,
) : ViewModel() {
    private val _selectedReadoutItemKey =
        MutableStateFlow<ReadoutItemKey>(
            ACE_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().first(),
        )
    private val _debugReadoutText = MutableStateFlow("")

    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases::saveKeys
    private val _receivedCardKeys = MutableStateFlow(setOf(AceWindowsDebugStateCardKey.SIMULATOR))

    private val aceWindowsSource =
        AceWindowsDebugStateSource(
            scope = viewModelScope,
            useCases = aceWindowsUseCases,
            markCardsReceived = ::markCardsReceived,
        )

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<AceWindowsDebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<AceWindowsDebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeKeys(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val detailFlow =
        AceWindowsDebugStateDetailFlow(
            cardOrder = _cardOrder,
            receivedCardKeys = _receivedCardKeys,
            aceWindows = aceWindowsSource.state,
            selectedReadoutItemKey = _selectedReadoutItemKey,
            debugReadoutText = _debugReadoutText,
        )

    private val _header: StateFlow<AceWindowsDebugStateDetailUiState> =
        detailFlow.header.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsDebugStateDetailUiState(),
        )

    val uiState: StateFlow<AceWindowsDebugStateDetailUiState> =
        detailFlow.uiState(_header).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsDebugStateDetailUiState(),
        )

    fun selectReadoutItem(value: String) {
        val key =
            ACE_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().find { it.value == value }
                ?: return
        _selectedReadoutItemKey.update { key }
    }

    fun updateDebugReadoutText(text: String) {
        _debugReadoutText.update { text }
    }

    fun requestReadout() {
        val text = _debugReadoutText.value
        if (text.isBlank()) return
        val request = DebugReadoutRequest(Simulator.AceWindows, _selectedReadoutItemKey.value, text)
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

    private fun markCardsReceived(cardKeys: Set<AceWindowsDebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + cardKeys
        }
    }
}

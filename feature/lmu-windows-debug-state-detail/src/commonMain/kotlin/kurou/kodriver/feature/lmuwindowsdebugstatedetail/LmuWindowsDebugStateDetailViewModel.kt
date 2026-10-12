package kurou.kodriver.feature.lmuwindowsdebugstatedetail

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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import kotlin.time.Clock

private const val SIDE_BY_SIDE_TICK_INTERVAL_MS = 100L

internal class LmuWindowsDebugStateDetailViewModel(
    lmuWindowsUseCases: LmuWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val requestDebugReadout: RequestDebugReadoutUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val sideBySideTickIntervalMs: Long = SIDE_BY_SIDE_TICK_INTERVAL_MS,
) : ViewModel() {
    private val _selectedReadoutItemKey =
        MutableStateFlow<ReadoutItemKey>(
            ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first(),
        )
    private val _debugReadoutText = MutableStateFlow("")

    private val resolveCardOrder = cardOrderUseCases.resolveCardOrder
    private val saveCardOrder = cardOrderUseCases::saveKeys
    private val _receivedCardKeys = MutableStateFlow(setOf(LmuWindowsDebugStateCardKey.SIMULATOR))

    private val lmuWindowsSource =
        LmuWindowsDebugStateSource(
            scope = viewModelScope,
            useCases = lmuWindowsUseCases,
            markCardsReceived = ::markCardsReceived,
            currentTimeMs = currentTimeMs,
            sideBySideTickIntervalMs = sideBySideTickIntervalMs,
        )

    // ドラッグ操作中はローカルの並び順を即座に UI へ反映し、DataStore への保存は非同期で行う。
    private val _localCardOrder = MutableStateFlow<List<LmuWindowsDebugStateCardKey>?>(null)
    private val _cardOrder: StateFlow<List<LmuWindowsDebugStateCardKey>> =
        combine(
            cardOrderUseCases.observeKeys(),
            _localCardOrder,
        ) { persisted, local ->
            local ?: resolveCardOrder(persistedOrder = persisted, defaultOrder = defaultDebugStateCardOrder)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val _header: StateFlow<LmuWindowsDebugStateDetailUiState> =
        combine(
            _cardOrder,
            _receivedCardKeys,
        ) { cardOrder, receivedCardKeys ->
            LmuWindowsDebugStateDetailUiState(
                enabledCardKeys = receivedCardKeys intersect lmuWindowsSupportedCardKeys,
                cardOrder = cardOrder,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsDebugStateDetailUiState())

    val uiState: StateFlow<LmuWindowsDebugStateDetailUiState> =
        combine(_header, lmuWindowsSource.state, _selectedReadoutItemKey, _debugReadoutText) { header, lmu, key, text ->
            header.copy(lmuWindows = lmu, selectedReadoutItemKey = key, debugReadoutText = text)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsDebugStateDetailUiState())

    fun selectReadoutItem(value: String) {
        val key = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).find { it.value == value } ?: return
        _selectedReadoutItemKey.update { key }
    }

    fun updateDebugReadoutText(text: String) {
        _debugReadoutText.update { text }
    }

    fun requestReadout() {
        val text = _debugReadoutText.value
        if (text.isBlank()) return
        val request = DebugReadoutRequest(Simulator.LmuWindows, _selectedReadoutItemKey.value, text)
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

    private fun markCardsReceived(cardKeys: Set<LmuWindowsDebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + cardKeys
        }
    }
}

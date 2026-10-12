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
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.time.Clock

internal class Gt7Ps5DebugStateDetailViewModel(
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val _debugOverlayInput = MutableStateFlow(Gt7Ps5DebugStateDetailUiState())

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
        combine(_header, gt7Ps5Source.state, _debugOverlayInput) { header, gt7Ps5, input ->
            header.copy(
                gt7Ps5 = gt7Ps5,
                debugOverlayText = input.debugOverlayText,
                debugOverlaySelectedKey = input.debugOverlaySelectedKey,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Gt7Ps5DebugStateDetailUiState())

    fun onDebugOverlayTextChange(text: String) {
        _debugOverlayInput.update { it.copy(debugOverlayText = text) }
    }

    fun onDebugOverlayKeySelect(key: ReadoutItemKey) {
        if (key !in _debugOverlayInput.value.debugOverlayKeys) return
        _debugOverlayInput.update { it.copy(debugOverlaySelectedKey = key) }
    }

    /**
     * 音声を鳴らさず、同一プロセスのオーバーレイへ入力文を届ける。
     * オーバーレイの購読は SKIPPED を除外するため、読み上げ済みの SPOKEN として保存する。
     */
    fun sendDebugOverlayText() {
        val input = _debugOverlayInput.value
        val text = input.debugOverlayText.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            saveTelemetryLog(
                createdAt = currentTimeMs(),
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = input.debugOverlaySelectedKey,
                narratedText = text,
                narrationOutcome = NarrationOutcome.SPOKEN,
                telemetryJson = "{}",
            )
            _debugOverlayInput.update { it.copy(debugOverlayText = "") }
        }
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

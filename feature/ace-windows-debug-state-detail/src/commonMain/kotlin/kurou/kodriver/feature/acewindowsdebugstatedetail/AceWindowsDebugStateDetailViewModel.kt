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
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.time.Clock

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
    private val saveTelemetryLog: SaveTelemetryLogUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val _debugOverlayInput = MutableStateFlow(AceWindowsDebugStateDetailUiState())

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
        combine(_header, aceWindowsSource.state, _debugOverlayInput) { header, ace, input ->
            header.copy(
                aceWindows = ace,
                debugOverlayText = input.debugOverlayText,
                debugOverlaySelectedKey = input.debugOverlaySelectedKey,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AceWindowsDebugStateDetailUiState())

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
                simulator = Simulator.AceWindows,
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

    private fun markCardsReceived(cardKeys: Set<AceWindowsDebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys ->
            receivedCardKeys + cardKeys
        }
    }
}

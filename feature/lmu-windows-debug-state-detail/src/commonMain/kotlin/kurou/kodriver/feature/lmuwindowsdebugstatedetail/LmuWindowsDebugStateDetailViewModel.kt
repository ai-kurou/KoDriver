package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import kotlin.time.Clock

private const val SIDE_BY_SIDE_TICK_INTERVAL_MS = 100L

internal class LmuWindowsDebugStateDetailViewModel(
    lmuWindowsUseCases: LmuWindowsDebugStateUseCases,
    cardOrderUseCases: DebugStateCardOrderUseCases,
    private val requestDebugReadout: RequestDebugReadoutUseCase,
    private val hotkeyUseCases: ReadoutHotkeyUseCases,
    private val hotkeyCheckTimeoutMs: Long = 10_000L,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val sideBySideTickIntervalMs: Long = SIDE_BY_SIDE_TICK_INTERVAL_MS,
) : ViewModel() {
    private val _selectedReadoutItemKey =
        MutableStateFlow<ReadoutItemKey>(
            LMU_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().first(),
        )
    private val _debugReadoutText = MutableStateFlow("")
    private val _hotkeyCheckState = MutableStateFlow(HotkeyCheckState.Idle)
    private var hotkeyCheckJob: Job? = null
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
        LmuWindowsDebugStateCardOrderFlow(
            persistedOrder = cardOrderUseCases.observeKeys(),
            localOrder = _localCardOrder,
            resolveCardOrder = cardOrderUseCases.resolveCardOrder,
        ).order.stateIn(viewModelScope, SharingStarted.Eagerly, defaultDebugStateCardOrder)

    private val detailFlow =
        LmuWindowsDebugStateDetailFlow(
            cardOrder = _cardOrder,
            receivedCardKeys = _receivedCardKeys,
            lmuWindows = lmuWindowsSource.state,
            selectedReadoutItemKey = _selectedReadoutItemKey,
            debugReadoutText = _debugReadoutText,
            hotkeyInputs =
                LmuWindowsDebugStateHotkeyInputs(
                    virtualKeyCode = hotkeyUseCases.observeVirtualKeyCode(),
                    registrationState = hotkeyUseCases.observeRegistrationState(),
                    checkState = _hotkeyCheckState,
                ),
        )
    private val _hotkey: StateFlow<ReadoutHotkeyUiState> =
        detailFlow.hotkey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReadoutHotkeyUiState())
    private val _header: StateFlow<LmuWindowsDebugStateDetailUiState> =
        detailFlow.header.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsDebugStateDetailUiState(),
        )
    private val _detailState: StateFlow<LmuWindowsDebugStateDetailUiState> =
        detailFlow.detail(_header).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsDebugStateDetailUiState(),
        )
    val uiState: StateFlow<LmuWindowsDebugStateDetailUiState> =
        detailFlow.uiState(_detailState, _hotkey).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsDebugStateDetailUiState(),
        )

    fun selectHotkey(virtualKeyCode: Int) {
        hotkeyCheckJob?.cancel()
        _hotkeyCheckState.update { HotkeyCheckState.Idle }
        viewModelScope.launch {
            hotkeyUseCases.saveVirtualKeyCode(virtualKeyCode)
            hotkeyUseCases.retryRegistration()
        }
    }

    fun checkHotkey() {
        hotkeyCheckJob?.cancel()
        _hotkeyCheckState.update { HotkeyCheckState.Waiting }
        hotkeyCheckJob =
            viewModelScope.launch {
                val detected = withTimeoutOrNull(hotkeyCheckTimeoutMs) { hotkeyUseCases.observePress().first() }
                _hotkeyCheckState.update {
                    if (detected !=
                        null
                    ) {
                        HotkeyCheckState.Detected
                    } else {
                        HotkeyCheckState.TimedOut
                    }
                }
            }
    }

    fun selectReadoutItem(value: String) {
        val key =
            LMU_WINDOWS_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().find { it.value == value }
                ?: return
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

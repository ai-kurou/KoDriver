package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class Gt7Ps5DebugStateDetailViewModel(
    gt7Ps5UseCases: Gt7Ps5DebugStateUseCases,
) : ViewModel() {
    private val _receivedCardKeys = MutableStateFlow(setOf(Gt7Ps5DebugStateCardKey.SIMULATOR))
    private val gt7Ps5Source =
        Gt7Ps5DebugStateSource(
            scope = viewModelScope,
            useCases = gt7Ps5UseCases,
            markCardsReceived = ::markCardsReceived,
        )

    val uiState: StateFlow<Gt7Ps5DebugStateDetailUiState> =
        combine(_receivedCardKeys, gt7Ps5Source.state) { receivedCardKeys, gt7Ps5 ->
            Gt7Ps5DebugStateDetailUiState(
                gt7Ps5 = gt7Ps5,
                enabledCardKeys = receivedCardKeys intersect gt7Ps5SupportedCardKeys,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Gt7Ps5DebugStateDetailUiState())

    private fun markCardsReceived(cardKeys: Set<Gt7Ps5DebugStateCardKey>) {
        _receivedCardKeys.update { receivedCardKeys -> receivedCardKeys + cardKeys }
    }
}

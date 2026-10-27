package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.gt7ps5.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

/**
 * ConnectionBanner 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class ConnectionBannerViewModel(
    checkLmuConnection: LmuBannerConnectionChecker,
    checkAceConnection: AceBannerConnectionChecker,
    checkGt7Ps5Connection: CheckGt7Ps5ConnectionUseCase,
    observeConsoleAddress: ObserveConsoleAddressUseCase,
    observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    val uiState: StateFlow<ConnectionBannerVmUiState> =
        ConnectionBannerFlow(
            selectedSimulator = observeSelectedSimulator(),
            observeConsoleAddress = { observeConsoleAddress() },
            checkLmuConnection = checkLmuConnection,
            checkAceConnection = checkAceConnection,
            checkGt7Ps5Connection = { checkGt7Ps5Connection() },
        ).uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ConnectionBannerVmUiState(),
        )
}

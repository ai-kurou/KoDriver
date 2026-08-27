package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

/**
 * ConnectionBanner 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class ConnectionBannerViewModel(
    private val checkLmuConnection: LmuBannerConnectionChecker,
    private val checkAceConnection: AceBannerConnectionChecker,
    private val checkGt7Ps5Connection: CheckGt7Ps5ConnectionUseCase,
    private val observeConsoleAddress: ObserveConsoleAddressUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ConnectionBannerVmUiState> =
        observeSelectedSimulator()
            .flatMapLatest { simulator ->
                when (simulator) {
                    is Simulator.LmuWindows -> {
                        checkLmuConnection
                            .statusFlow()
                            .map { ConnectionBannerVmUiState(it, simulator) }
                    }

                    is Simulator.Gt7Ps5 -> {
                        gt7ConnectionFlow(simulator)
                    }

                    is Simulator.AceWindows -> {
                        checkAceConnection
                            .statusFlow()
                            .map { ConnectionBannerVmUiState(it, simulator) }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = ConnectionBannerVmUiState(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun gt7ConnectionFlow(simulator: Simulator) =
        observeConsoleAddress()
            .flatMapLatest { address ->
                if (address == null) {
                    flowOf(ConnectionBannerVmUiState(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, simulator))
                } else {
                    connectionCheckFlow(simulator) { checkGt7Ps5Connection() }
                }
            }

    private fun connectionCheckFlow(
        simulator: Simulator,
        check: suspend () -> Boolean,
    ) = flow {
        while (true) {
            val isConnected =
                try {
                    check()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    false
                }
            emit(
                ConnectionBannerVmUiState(
                    connectionStatus =
                        if (isConnected) {
                            ConnectionBannerVmStatus.CONNECTED
                        } else {
                            ConnectionBannerVmStatus.DISCONNECTED
                        },
                    selectedSimulator = simulator,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
        }
    }
}

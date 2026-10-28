package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator

/** 選択中のシミュレーターの接続状態を生成する冷たい Flow。購読の寿命は呼び出し元が管理する。 */
internal class ConnectionBannerFlow(
    selectedSimulator: Flow<Simulator>,
    private val observeConsoleAddress: () -> Flow<String?>,
    private val checkLmuConnection: LmuBannerConnectionChecker,
    private val checkAceConnection: AceBannerConnectionChecker,
    private val checkGt7Ps5Connection: suspend () -> Boolean,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: Flow<ConnectionBannerVmUiState> =
        selectedSimulator
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
            }

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

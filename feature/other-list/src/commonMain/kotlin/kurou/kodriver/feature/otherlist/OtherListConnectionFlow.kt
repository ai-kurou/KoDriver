package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

/** 接続情報の観測と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListConnectionFlow(
    observeServerIp: ObserveServerIpUseCase?,
    observeConsoleAddress: ObserveConsoleAddressUseCase,
    observeGt7Ps5UdpPort: ObserveGt7Ps5UdpPortUseCase,
) {
    private val serverIpFlow =
        if (observeServerIp == null) {
            flowOf(null)
        } else {
            observeServerIp()
        }

    val uiState: Flow<OtherListConnectionUiState> =
        combine(
            serverIpFlow,
            observeConsoleAddress(),
            observeGt7Ps5UdpPort(),
        ) { serverIp, consoleAddress, consolePort ->
            OtherListConnectionUiState(serverIp, consoleAddress, consolePort)
        }
}

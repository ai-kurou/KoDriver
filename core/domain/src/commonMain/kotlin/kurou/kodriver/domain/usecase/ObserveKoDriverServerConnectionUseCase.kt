package kurou.kodriver.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator

enum class KoDriverServerConnectionStatus {
    NOT_CONFIGURED,
    CHECKING,
    CONNECTED,
    DISCONNECTED,
}

data class KoDriverServerConnectionState(
    val connectionStatus: KoDriverServerConnectionStatus = KoDriverServerConnectionStatus.NOT_CONFIGURED,
    val requiresKoDriverServer: Boolean = false,
    val selectedSimulator: Simulator = Simulator.LmuWindows,
    val serverVersion: String? = null,
    val isVersionMismatch: Boolean = false,
)

class ObserveKoDriverServerConnectionUseCase(
    private val fetchServerVersion: FetchServerVersionUseCase,
    private val observeServerIp: ObserveServerIpUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(appVersion: String): Flow<KoDriverServerConnectionState> =
        combine(
            observeServerIp(),
            observeSelectedSimulator(),
        ) { ip, simulator -> ip to simulator }
            .flatMapLatest { (ip, simulator) ->
                val requiresServer = simulator.requiresKoDriverServer
                if (ip != null) {
                    connectionCheckFlow(
                        ip = ip,
                        simulator = simulator,
                        requiresServer = requiresServer,
                        appVersion = appVersion,
                    )
                } else {
                    flowOf(
                        KoDriverServerConnectionState(
                            connectionStatus = KoDriverServerConnectionStatus.NOT_CONFIGURED,
                            requiresKoDriverServer = requiresServer,
                            selectedSimulator = simulator,
                        ),
                    )
                }
            }

    private fun connectionCheckFlow(
        ip: String,
        simulator: Simulator,
        requiresServer: Boolean,
        appVersion: String,
    ) = flow {
        emit(
            KoDriverServerConnectionState(
                connectionStatus = KoDriverServerConnectionStatus.CHECKING,
                requiresKoDriverServer = requiresServer,
                selectedSimulator = simulator,
            ),
        )
        while (true) {
            val versionResult = fetchServerVersion(ip)
            val serverVersion = versionResult.getOrNull()
            emit(
                KoDriverServerConnectionState(
                    connectionStatus =
                        if (versionResult.isSuccess) {
                            KoDriverServerConnectionStatus.CONNECTED
                        } else {
                            KoDriverServerConnectionStatus.DISCONNECTED
                        },
                    requiresKoDriverServer = requiresServer,
                    selectedSimulator = simulator,
                    serverVersion = serverVersion,
                    isVersionMismatch = serverVersion != null && appVersion.isNotEmpty() && serverVersion != appVersion,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
        }
    }
}

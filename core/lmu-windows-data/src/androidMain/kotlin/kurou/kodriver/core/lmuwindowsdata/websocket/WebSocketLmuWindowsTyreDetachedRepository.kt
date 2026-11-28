package kurou.kodriver.core.lmuwindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.model.KODRIVER_SERVER_PORT_DEFAULT
import kurou.kodriver.domain.model.KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository

internal class WebSocketLmuWindowsTyreDetachedRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = KODRIVER_SERVER_PORT_DEFAULT,
    private val retryDelayMs: Long = KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT,
    private val client: HttpClient = createWebSocketHttpClient(),
) : LmuWindowsTyreDetachedRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.TYRE_DETACHED.webSocketPath(Simulator.LmuWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { webSocketJson.decodeFromString<LmuWindowsTyreDetachedData>(it) },
                    )
                }
            }
}

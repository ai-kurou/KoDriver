package kurou.kodriver.core.lmuwindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.model.KODRIVER_SERVER_PORT_DEFAULT
import kurou.kodriver.domain.model.KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

internal class WebSocketLmuWindowsFlagRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = KODRIVER_SERVER_PORT_DEFAULT,
    private val retryDelayMs: Long = KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT,
    private val client: HttpClient = createWebSocketHttpClient(),
) : LmuWindowsFlagRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { json.decodeFromString<LmuWindowsRaceFlagsData>(it) },
                    )
                }
            }
}

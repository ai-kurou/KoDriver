package kurou.kodriver.core.acewindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.model.KODRIVER_SERVER_PORT_DEFAULT
import kurou.kodriver.domain.model.KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

internal class WebSocketAceWindowsFlagRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = KODRIVER_SERVER_PORT_DEFAULT,
    private val retryDelayMs: Long = KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT,
    private val client: HttpClient = createWebSocketHttpClient(),
) : AceWindowsFlagRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun flagStream(): Flow<AceWindowsFlagData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.FLAGS.webSocketPath(Simulator.AceWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { json.decodeFromString<AceWindowsFlagData>(it) },
                    )
                }
            }
}

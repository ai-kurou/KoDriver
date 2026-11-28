package kurou.kodriver.core.acewindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.model.KODRIVER_SERVER_PORT_DEFAULT
import kurou.kodriver.domain.model.KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository

internal class WebSocketAceWindowsTyreCarcassTemperatureRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = KODRIVER_SERVER_PORT_DEFAULT,
    private val retryDelayMs: Long = KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT,
    private val client: HttpClient = createWebSocketHttpClient(),
) : AceWindowsTyreCarcassTemperatureRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE.webSocketPath(Simulator.AceWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { webSocketJson.decodeFromString<AceWindowsTyreCarcassTemperatureData>(it) },
                    )
                }
            }
}

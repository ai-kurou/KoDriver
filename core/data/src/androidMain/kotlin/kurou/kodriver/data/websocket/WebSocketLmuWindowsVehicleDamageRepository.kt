package kurou.kodriver.data.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

private const val DEFAULT_PORT = 8080
private const val DEFAULT_RETRY_DELAY_MS = 3000L

internal class WebSocketLmuWindowsVehicleDamageRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = DEFAULT_PORT,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val client: HttpClient = createWebSocketHttpClient(),
) : LmuWindowsVehicleDamageRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.DAMAGE.webSocketPath(Simulator.LmuWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { json.decodeFromString<LmuWindowsVehicleDamageData>(it) },
                    )
                }
            }
}

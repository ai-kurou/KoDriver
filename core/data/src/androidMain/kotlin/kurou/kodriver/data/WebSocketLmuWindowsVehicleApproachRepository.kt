package kurou.kodriver.data

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

private const val DEFAULT_PORT = 8080
private const val DEFAULT_RETRY_DELAY_MS = 3000L

internal class WebSocketLmuWindowsVehicleApproachRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = DEFAULT_PORT,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val client: HttpClient = createWebSocketHttpClient(),
) : LmuWindowsVehicleApproachRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client.webSocketFlow(
                        host = ip,
                        port = port,
                        path = KoDriverServerFeature.VEHICLE_APPROACH.webSocketPath(Simulator.LmuWindows),
                        retryDelayMs = retryDelayMs,
                        decode = { json.decodeFromString<LmuWindowsVehicleApproachData>(it) },
                    )
                }
            }
}

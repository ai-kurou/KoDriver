package kurou.kodriver.core.lmuwindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_FUEL_MAX_AGE_MS
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.model.KODRIVER_SERVER_PORT_DEFAULT
import kurou.kodriver.domain.model.KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

private val emptyFuel =
    LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0))

internal class WebSocketLmuWindowsRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = KODRIVER_SERVER_PORT_DEFAULT,
    private val retryDelayMs: Long = KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT,
    private val client: HttpClient = createWebSocketHttpClient(),
    private val currentTimeMs: () -> Long = System::currentTimeMillis,
) : LmuWindowsRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> =
        serverIpRepository
            .serverIp()
            .flatMapLatest { ip ->
                if (ip == null) {
                    emptyFlow()
                } else {
                    client
                        .webSocketFlow(
                            host = ip,
                            port = port,
                            path = KoDriverServerFeature.TELEMETRY.webSocketPath(Simulator.LmuWindows),
                            retryDelayMs = retryDelayMs,
                            decode = {
                                val telemetry = json.decodeFromString<LmuWindowsTelemetryData>(it)
                                telemetry.copy(fuel = telemetry.fuel.copy(receivedAtMs = currentTimeMs()))
                            },
                        ).transformLatest { telemetry ->
                            emit(telemetry)
                            delay(LMU_WINDOWS_FUEL_MAX_AGE_MS)
                            emit(telemetry.copy(fuel = emptyFuel))
                        }
                }
            }

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit
}

package kurou.kodriver.core.lmuwindowsdata.websocket

import io.ktor.client.HttpClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsInputsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTimingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

private const val DEFAULT_PORT = 8080
private const val DEFAULT_RETRY_DELAY_MS = 3000L
private const val FUEL_MAX_AGE_MS = 5_000L

// サーバーは timing と fuel を配信し、他のフィールドはダミー値で埋める。
private val emptyEngine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0)
private val emptyInputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0)
private val emptyTyres = LmuWindowsTyreData(wheels = emptyMap())
private val emptyFuel =
    LmuWindowsFuelData(currentLiters = LmuWindowsFuelUnit(0.0), capacityLiters = LmuWindowsFuelUnit(0.0))
private val emptyVehicle =
    LmuWindowsVehicleData(
        localVelocityX = 0.0,
        localVelocityY = 0.0,
        localVelocityZ = 0.0,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    )

internal class WebSocketLmuWindowsRepository(
    private val serverIpRepository: ServerIpPreferencesRepository,
    private val port: Int = DEFAULT_PORT,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val client: HttpClient = createWebSocketHttpClient(),
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
                    combine(
                        client.webSocketFlow(
                            host = ip,
                            port = port,
                            path = KoDriverServerFeature.MY_BEST_LAP.webSocketPath(Simulator.LmuWindows),
                            retryDelayMs = retryDelayMs,
                            decode = { json.decodeFromString<LmuWindowsTimingData>(it) },
                        ),
                        client
                            .webSocketFlow(
                                host = ip,
                                port = port,
                                path = KoDriverServerFeature.FUEL.webSocketPath(Simulator.LmuWindows),
                                retryDelayMs = retryDelayMs,
                                decode = {
                                    json.decodeFromString<LmuWindowsFuelData>(it).copy(receivedAtMs = System.currentTimeMillis())
                                },
                            ).transformLatest { fuel ->
                                emit(fuel)
                                delay(FUEL_MAX_AGE_MS)
                                emit(emptyFuel)
                            }.onStart { emit(emptyFuel) },
                    ) { timing, fuel ->
                        LmuWindowsTelemetryData(
                            timestampMs = 0L,
                            engine = emptyEngine,
                            inputs = emptyInputs,
                            tyres = emptyTyres,
                            fuel = fuel,
                            timing = timing,
                            vehicle = emptyVehicle,
                        )
                    }
                }
            }

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit
}

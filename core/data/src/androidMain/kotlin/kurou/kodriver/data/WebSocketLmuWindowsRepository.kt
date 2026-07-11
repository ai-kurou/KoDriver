package kurou.kodriver.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

private const val DEFAULT_PORT = 8080
private const val DEFAULT_RETRY_DELAY_MS = 3000L

// サーバーは timing フィールドのみを配信するため、他のフィールドはダミー値で埋める。
// LmuWindowsNarratorViewModel など Narrator 側は timing フィールドしか参照しない。
private val emptyEngine = LmuWindowsEngineData(rpm = 0.0, maxRpm = 0.0, gear = 0)
private val emptyInputs = LmuWindowsInputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0)
private val emptyTyres = LmuWindowsTyreData(wheels = emptyMap())
private val emptyFuel = LmuWindowsFuelData(currentLiters = 0.0, capacityLiters = 0.0)
private val emptyVehicle = LmuWindowsVehicleData(
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
) : LmuWindowsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> =
        serverIpRepository.serverIp()
            .flatMapLatest { ip ->
                if (ip == null) emptyFlow()
                else connectWithRetry(ip)
            }
            .map { timing ->
                LmuWindowsTelemetryData(
                    timestampMs = 0L,
                    engine = emptyEngine,
                    inputs = emptyInputs,
                    tyres = emptyTyres,
                    fuel = emptyFuel,
                    timing = timing,
                    vehicle = emptyVehicle,
                )
            }

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit

    private fun connectWithRetry(ip: String): Flow<LmuWindowsTimingData> = flow {
        while (true) {
            try {
                client.webSocket(
                    host = ip,
                    port = port,
                    path = KoDriverServerFeature.MY_BEST_LAP.webSocketPath(Simulator.LmuWindows),
                ) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            try {
                                emit(json.decodeFromString<LmuWindowsTimingData>(frame.readText()))
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: SerializationException) {
                                Sentry.captureException(e)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
            delay(retryDelayMs)
        }
    }
}

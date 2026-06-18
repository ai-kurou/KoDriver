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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.ServerIpRepository

private const val DEFAULT_PORT = 8080
private const val PATH = "/ws/lmu_windows/flags"
private const val DEFAULT_RETRY_DELAY_MS = 3000L

internal class WebSocketFlagRepository(
    private val serverIpRepository: ServerIpRepository,
    private val port: Int = DEFAULT_PORT,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
) : FlagRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun flagStream(): Flow<RaceFlagsData> =
        serverIpRepository.serverIp()
            .flatMapLatest { ip ->
                if (ip == null) emptyFlow()
                else connectWithRetry(ip)
            }

    private fun connectWithRetry(ip: String): Flow<RaceFlagsData> = flow {
        while (true) {
            try {
                client.webSocket(host = ip, port = port, path = PATH) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            try {
                                emit(json.decodeFromString<RaceFlagsData>(frame.readText()))
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

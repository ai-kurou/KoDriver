package kurou.kodriver

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import org.koin.core.Koin

fun main() {
    KoDriverServer(ObserveRaceFlagsUseCase(EmptyFlagRepository)).start(wait = true)
}

class KoDriverServer(
    observeRaceFlags: ObserveRaceFlagsUseCase,
    port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    private val server = embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = {
            module(observeRaceFlags)
        },
    )

    fun start(wait: Boolean = false) {
        server.start(wait = wait)
    }

    fun stop() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }

    private companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOST = "0.0.0.0"
    }
}

fun createKoDriverServer(koin: Koin): KoDriverServer {
    val repository = koin.get<FlagRepository>()
    return KoDriverServer(ObserveRaceFlagsUseCase(repository))
}

fun Application.module(observeRaceFlags: ObserveRaceFlagsUseCase) {
    install(WebSockets)
    routing {
        get("/") {
            call.respondText("Hello, Ktor!")
        }
        flagWebSocket(observeRaceFlags)
    }
}

private object EmptyFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}

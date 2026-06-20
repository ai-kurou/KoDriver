package kurou.kodriver

import io.ktor.http.ContentType
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
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase
import org.koin.core.Koin

fun main() {
    KoDriverServer(
        observeRaceFlags = ObserveRaceFlagsUseCase(EmptyFlagRepository),
        observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
        observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
    ).start(wait = true)
}

class KoDriverServer(
    observeRaceFlags: ObserveRaceFlagsUseCase,
    observeProximity: ObserveProximityUseCase,
    observeVehicleDamage: ObserveVehicleDamageUseCase,
    port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    private val server = embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = {
            module(observeRaceFlags, observeProximity, observeVehicleDamage)
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
    return KoDriverServer(
        observeRaceFlags = ObserveRaceFlagsUseCase(koin.get<FlagRepository>()),
        observeProximity = ObserveProximityUseCase(koin.get<ProximityRepository>()),
        observeVehicleDamage = ObserveVehicleDamageUseCase(koin.get<VehicleDamageRepository>()),
    )
}

fun Application.module(
    observeRaceFlags: ObserveRaceFlagsUseCase,
    observeProximity: ObserveProximityUseCase,
    observeVehicleDamage: ObserveVehicleDamageUseCase,
) {
    install(WebSockets)
    routing {
        get("/") {
            call.respondText("Hello, Ktor!")
        }
        get("/version") {
            call.respondText(
                """{"version":"${BuildConfig.APP_VERSION}"}""",
                ContentType.Application.Json,
            )
        }
        flagWebSocket(observeRaceFlags)
        proximityWebSocket(observeProximity)
        vehicleDamageWebSocket(observeVehicleDamage)
    }
}

private object EmptyFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}

private object EmptyProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

private object EmptyVehicleDamageRepository : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = emptyFlow()
}

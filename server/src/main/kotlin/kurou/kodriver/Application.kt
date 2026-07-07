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
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsProximityUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import org.koin.core.Koin

fun main() {
    KoDriverServer(
        observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(EmptyFlagRepository),
        observeProximity = ObserveLmuWindowsProximityUseCase(EmptyProximityRepository),
        observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
        observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
            EmptyTyreCarcassTemperatureRepository,
        ),
        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
    ).start(wait = true)
}

class KoDriverServer(
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeProximity: ObserveLmuWindowsProximityUseCase,
    observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    observeLmuWindows: ObserveLmuWindowsUseCase,
    port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    private val server = embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = {
            module(
                observeRaceFlags,
                observeProximity,
                observeVehicleDamage,
                observeTyreCarcassTemperature,
                observeLmuWindows,
            )
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
        observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(koin.get<LmuWindowsFlagRepository>()),
        observeProximity = ObserveLmuWindowsProximityUseCase(koin.get<LmuWindowsProximityRepository>()),
        observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(koin.get<LmuWindowsVehicleDamageRepository>()),
        observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
            koin.get<LmuWindowsTyreCarcassTemperatureRepository>(),
        ),
        observeLmuWindows = ObserveLmuWindowsUseCase(koin.get<LmuWindowsRepository>()),
    )
}

fun Application.module(
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    observeProximity: ObserveLmuWindowsProximityUseCase,
    observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    observeLmuWindows: ObserveLmuWindowsUseCase,
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
        tyreCarcassTemperatureWebSocket(observeTyreCarcassTemperature)
        timingWebSocket(observeLmuWindows)
    }
}

private object EmptyFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private object EmptyProximityRepository : LmuWindowsProximityRepository {
    override fun proximityStream(): Flow<LmuWindowsProximityData> = emptyFlow()
}

private object EmptyVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private object EmptyTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

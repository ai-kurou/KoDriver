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
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import org.koin.core.Koin

/**
 * `:server` が配信する各 WebSocket エンドポイントの購読用 UseCase をまとめたバンドル。
 *
 * [KoDriverServer] / [Application.module] のコンストラクタ・関数パラメータ数を detekt の
 * LongParameterList 閾値内に収めるため、個別のコンストラクタ引数ではなくこのデータクラスで受け渡す。
 */
data class KoDriverServerUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
)

fun main() {
    KoDriverServer(
        useCases = KoDriverServerUseCases(
            observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(EmptyFlagRepository),
            observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(EmptyVehicleApproachRepository),
            observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(EmptyVehicleDamageRepository),
            observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                EmptyTyreCarcassTemperatureRepository,
            ),
            observeTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyTyreWearRepository),
            observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(EmptyVirtualEnergyRepository),
            observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
            observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
        ),
    ).start(wait = true)
}

class KoDriverServer(
    useCases: KoDriverServerUseCases,
    private val port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    internal var serviceAdvertiser: KoDriverServiceAdvertiser = KoDriverServiceAdvertiser()
    private val server = embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = {
            module(useCases)
        },
    )

    fun start(wait: Boolean = false) {
        server.start(wait = wait)
        serviceAdvertiser.start(port)
    }

    fun stop() {
        serviceAdvertiser.stop()
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }

    private companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOST = "0.0.0.0"
    }
}

fun createKoDriverServer(koin: Koin): KoDriverServer {
    return KoDriverServer(
        useCases = KoDriverServerUseCases(
            observeRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(koin.get<LmuWindowsFlagRepository>()),
            observeVehicleApproach = ObserveLmuWindowsVehicleApproachUseCase(
                koin.get<LmuWindowsVehicleApproachRepository>(),
            ),
            observeVehicleDamage = ObserveLmuWindowsVehicleDamageUseCase(
                koin.get<LmuWindowsVehicleDamageRepository>(),
            ),
            observeTyreCarcassTemperature = ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                koin.get<LmuWindowsTyreCarcassTemperatureRepository>(),
            ),
            observeTyreWear = ObserveLmuWindowsTyreWearUseCase(koin.get<LmuWindowsTyreWearRepository>()),
            observeLmuWindows = ObserveLmuWindowsUseCase(koin.get<LmuWindowsRepository>()),
            observeVirtualEnergy = ObserveLmuWindowsVirtualEnergyUseCase(
                koin.get<LmuWindowsVirtualEnergyRepository>(),
            ),
            observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(koin.get<AceWindowsFuelRepository>()),
            observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(koin.get<AceWindowsFlagRepository>()),
        ),
    )
}

private const val WEB_SOCKET_PING_PERIOD_MS = 15_000L
private const val WEB_SOCKET_TIMEOUT_MS = 15_000L

fun Application.module(useCases: KoDriverServerUseCases) {
    install(WebSockets) {
        // クライアントがサイレントに消えた（half-open になった）接続を検知して
        // セッションを解放するため、ping/pong を有効にする。
        pingPeriodMillis = WEB_SOCKET_PING_PERIOD_MS
        timeoutMillis = WEB_SOCKET_TIMEOUT_MS
    }
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
        flagWebSocket(useCases.observeRaceFlags)
        vehicleApproachWebSocket(useCases.observeVehicleApproach)
        vehicleDamageWebSocket(useCases.observeVehicleDamage)
        tyreCarcassTemperatureWebSocket(useCases.observeTyreCarcassTemperature)
        tyreWearWebSocket(useCases.observeTyreWear)
        timingWebSocket(useCases.observeLmuWindows)
        virtualEnergyWebSocket(useCases.observeVirtualEnergy)
        aceWindowsFuelWebSocket(useCases.observeAceWindowsFuel)
        aceWindowsFlagWebSocket(useCases.observeAceWindowsFlag)
    }
}

private object EmptyFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private object EmptyVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private object EmptyTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private object EmptyVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private object EmptyAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
}

private object EmptyAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

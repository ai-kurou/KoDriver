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
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
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
    val observeLmuWindowsRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeLmuWindowsVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindowsVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeLmuWindowsTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeLmuWindowsTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeLmuWindowsVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
)

fun main() {
    KoDriverServer(
        useCases =
            KoDriverServerUseCases(
                observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(EmptyLmuWindowsFlagRepository),
                observeLmuWindowsVehicleApproach =
                    ObserveLmuWindowsVehicleApproachUseCase(
                        EmptyLmuWindowsVehicleApproachRepository,
                    ),
                observeLmuWindowsVehicleDamage =
                    ObserveLmuWindowsVehicleDamageUseCase(
                        EmptyLmuWindowsVehicleDamageRepository,
                    ),
                observeLmuWindowsTyreCarcassTemperature =
                    ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        EmptyLmuWindowsTyreCarcassTemperatureRepository,
                    ),
                observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
                observeLmuWindowsVirtualEnergy =
                    ObserveLmuWindowsVirtualEnergyUseCase(
                        EmptyLmuWindowsVirtualEnergyRepository,
                    ),
                observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
                observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
                observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
            ),
    ).start(wait = true)
}

/**
 * KoDriver の WebSocket サーバー。
 *
 * Desktop アプリ内で起動し、LMU / ACE など Windows 側でしか読めない走行情報を
 * LAN 内の Android アプリへ配信する。既定では `0.0.0.0:8080` で待ち受け、
 * 起動時に mDNS で KoDriver サービスを広告する。
 */
class KoDriverServer(
    useCases: KoDriverServerUseCases,
    private val port: Int = DEFAULT_PORT,
    host: String = DEFAULT_HOST,
) {
    internal var serviceAdvertiser: KoDriverServiceAdvertiser = KoDriverServiceAdvertiser()
    private val server =
        embeddedServer(
            factory = Netty,
            port = port,
            host = host,
            module = {
                module(useCases)
            },
        )

    /**
     * Ktor サーバーを起動し、同じポートで mDNS 広告を開始する。
     *
     * [wait] を true にすると Ktor のサーバースレッドでブロックするため、
     * CLI エントリーポイントから単体起動する場合に使う。
     */
    fun start(wait: Boolean = false) {
        server.start(wait = wait)
        serviceAdvertiser.start(port)
    }

    /** mDNS 広告を停止し、Ktor サーバーを即時停止する。 */
    fun stop() {
        serviceAdvertiser.stop()
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }

    private companion object {
        const val DEFAULT_PORT = 8080
        const val DEFAULT_HOST = "0.0.0.0"
    }
}

/**
 * アプリ本体の Koin コンテナから Repository を解決して [KoDriverServer] を生成する。
 *
 * Desktop アプリ内でサーバーを起動する通常経路。単体起動用の [main] は空 Repository を使うため、
 * 実走行データを配信したい場合はこちらを使う。
 */
fun createKoDriverServer(koin: Koin): KoDriverServer =
    KoDriverServer(
        useCases =
            KoDriverServerUseCases(
                observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(koin.get<LmuWindowsFlagRepository>()),
                observeLmuWindowsVehicleApproach =
                    ObserveLmuWindowsVehicleApproachUseCase(
                        koin.get<LmuWindowsVehicleApproachRepository>(),
                    ),
                observeLmuWindowsVehicleDamage =
                    ObserveLmuWindowsVehicleDamageUseCase(
                        koin.get<LmuWindowsVehicleDamageRepository>(),
                    ),
                observeLmuWindowsTyreCarcassTemperature =
                    ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                        koin.get<LmuWindowsTyreCarcassTemperatureRepository>(),
                    ),
                observeLmuWindowsTyreWear =
                    ObserveLmuWindowsTyreWearUseCase(
                        koin.get<LmuWindowsTyreWearRepository>(),
                    ),
                observeLmuWindows = ObserveLmuWindowsUseCase(koin.get<LmuWindowsRepository>()),
                observeLmuWindowsVirtualEnergy =
                    ObserveLmuWindowsVirtualEnergyUseCase(
                        koin.get<LmuWindowsVirtualEnergyRepository>(),
                    ),
                observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(koin.get<AceWindowsFuelRepository>()),
                observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(koin.get<AceWindowsFlagRepository>()),
                observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(koin.get<AceWindowsStatusRepository>()),
            ),
    )

private const val WEB_SOCKET_PING_PERIOD_MS = 15_000L
private const val WEB_SOCKET_TIMEOUT_MS = 15_000L

/**
 * KoDriver サーバーの Ktor module。
 *
 * `/version` はアプリバージョンを JSON で返し、`/ws/{simulator}/{feature}` 系の
 * WebSocket エンドポイントは [KoDriverServerUseCases] の Flow を JSON メッセージとして配信する。
 */
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
        flagWebSocket(useCases.observeLmuWindowsRaceFlags)
        vehicleApproachWebSocket(useCases.observeLmuWindowsVehicleApproach)
        vehicleDamageWebSocket(useCases.observeLmuWindowsVehicleDamage)
        tyreCarcassTemperatureWebSocket(useCases.observeLmuWindowsTyreCarcassTemperature)
        tyreWearWebSocket(useCases.observeLmuWindowsTyreWear)
        timingWebSocket(useCases.observeLmuWindows)
        virtualEnergyWebSocket(useCases.observeLmuWindowsVirtualEnergy)
        aceWindowsFuelWebSocket(useCases.observeAceWindowsFuel)
        aceWindowsFlagWebSocket(useCases.observeAceWindowsFlag)
        aceWindowsStatusWebSocket(useCases.observeAceWindowsStatus)
    }
}

private object EmptyLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private object EmptyLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit
}

private object EmptyLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private object EmptyAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false
}

private object EmptyAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

private object EmptyAceWindowsStatusRepository : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = emptyFlow()
}

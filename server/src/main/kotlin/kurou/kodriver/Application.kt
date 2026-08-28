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
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
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
    val observeLmuWindowsVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observeLmuWindowsTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeLmuWindowsVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
    val observeAceWindowsTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeAceWindowsVehicleApproach: ObserveAceWindowsVehicleApproachUseCase,
    val observeAceWindowsBestLapTime: ObserveAceWindowsBestLapTimeUseCase,
    val observeLmuWindowsPitStatus: ObserveLmuWindowsPitStatusUseCase,
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
                observeLmuWindowsVehicleClass =
                    ObserveLmuWindowsVehicleClassUseCase(
                        EmptyLmuWindowsVehicleClassRepository,
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
                observeAceWindowsTyreCarcassTemperature =
                    ObserveAceWindowsTyreCarcassTemperatureUseCase(
                        EmptyAceWindowsTyreCarcassTemperatureRepository,
                    ),
                observeAceWindowsVehicleApproach =
                    ObserveAceWindowsVehicleApproachUseCase(
                        EmptyAceWindowsVehicleApproachRepository,
                    ),
                observeAceWindowsBestLapTime =
                    ObserveAceWindowsBestLapTimeUseCase(
                        EmptyAceWindowsBestLapTimeRepository,
                    ),
                observeLmuWindowsPitStatus =
                    ObserveLmuWindowsPitStatusUseCase(
                        EmptyLmuWindowsPitStatusRepository,
                    ),
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
                observeLmuWindowsVehicleClass =
                    ObserveLmuWindowsVehicleClassUseCase(
                        koin.get<LmuWindowsVehicleClassRepository>(),
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
                observeAceWindowsTyreCarcassTemperature =
                    ObserveAceWindowsTyreCarcassTemperatureUseCase(
                        koin.get<AceWindowsTyreCarcassTemperatureRepository>(),
                    ),
                observeAceWindowsVehicleApproach =
                    ObserveAceWindowsVehicleApproachUseCase(
                        koin.get<AceWindowsVehicleApproachRepository>(),
                    ),
                observeAceWindowsBestLapTime =
                    ObserveAceWindowsBestLapTimeUseCase(
                        koin.get<AceWindowsBestLapTimeRepository>(),
                    ),
                observeLmuWindowsPitStatus =
                    ObserveLmuWindowsPitStatusUseCase(
                        koin.get<LmuWindowsPitStatusRepository>(),
                    ),
            ),
    )

private const val WEB_SOCKET_PING_PERIOD_MS = 15_000L
private const val WEB_SOCKET_TIMEOUT_MS = 15_000L
private const val WEB_SOCKET_MAX_FRAME_SIZE_BYTES = 64L * 1024

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
        // LAN内の端末から巨大なフレームを送られた場合の受信バッファ肥大化を防ぐ。
        maxFrameSize = WEB_SOCKET_MAX_FRAME_SIZE_BYTES
    }
    routing {
        get("/") {
            call.respondText(
                """{"status":"ok"}""",
                ContentType.Application.Json,
            )
        }
        get("/version") {
            call.respondText(
                """{"version":"${BuildConfig.APP_VERSION}"}""",
                ContentType.Application.Json,
            )
        }
        telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.LmuWindows) {
            useCases.observeLmuWindowsRaceFlags()
        }
        telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.LmuWindows) {
            useCases.observeLmuWindowsVehicleApproach()
        }
        telemetryWebSocket(KoDriverServerFeature.DAMAGE, Simulator.LmuWindows) {
            useCases.observeLmuWindowsVehicleDamage()
        }
        telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.LmuWindows) {
            useCases.observeLmuWindowsTyreCarcassTemperature()
        }
        telemetryWebSocket(KoDriverServerFeature.VEHICLE_CLASS, Simulator.LmuWindows) {
            useCases.observeLmuWindowsVehicleClass()
        }
        telemetryWebSocket(KoDriverServerFeature.TYRE_WEAR, Simulator.LmuWindows) {
            useCases.observeLmuWindowsTyreWear()
        }
        telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.LmuWindows) {
            useCases.observeLmuWindows().map { it.timing }
        }
        telemetryWebSocket(KoDriverServerFeature.VIRTUAL_ENERGY, Simulator.LmuWindows) {
            useCases.observeLmuWindowsVirtualEnergy()
        }
        telemetryWebSocket(KoDriverServerFeature.FUEL, Simulator.AceWindows) {
            useCases.observeAceWindowsFuel()
        }
        telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.AceWindows) {
            useCases.observeAceWindowsFlag()
        }
        telemetryWebSocket(KoDriverServerFeature.STATUS, Simulator.AceWindows) {
            useCases.observeAceWindowsStatus()
        }
        telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.AceWindows) {
            useCases.observeAceWindowsTyreCarcassTemperature()
        }
        telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.AceWindows) {
            useCases.observeAceWindowsVehicleApproach()
        }
        telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.AceWindows) {
            useCases.observeAceWindowsBestLapTime()
        }
        telemetryWebSocket(KoDriverServerFeature.PIT_STATUS, Simulator.LmuWindows) {
            useCases.observeLmuWindowsPitStatus()
        }
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

private object EmptyLmuWindowsVehicleClassRepository : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = emptyFlow()
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

private object EmptyAceWindowsTyreCarcassTemperatureRepository : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyAceWindowsVehicleApproachRepository : AceWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyAceWindowsBestLapTimeRepository : AceWindowsBestLapTimeRepository {
    override fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData> = emptyFlow()
}

private object EmptyLmuWindowsPitStatusRepository : LmuWindowsPitStatusRepository {
    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = emptyFlow()
}

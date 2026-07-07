package kurou.kodriver

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.TyreWheelData
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.model.VehicleData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.net.ServerSocket
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    @Test
    fun `バージョンエンドポイントはアプリバージョンをJSONで返す`() = testApplication {
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }
        val response = client.get("/version")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"version":"${BuildConfig.APP_VERSION}"}""", response.bodyAsText())
    }

    @Test
    fun `ルートはサーバーの応答を返す`() = testApplication {
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun `フラッグ情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeFlagRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(repository),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/flags") {
            repository.emit(greenFlagData)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE","sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
                    """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
                    """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}""",
                message,
            )
        }
    }

    @Test
    fun `フラッグ情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeFlagRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(repository),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/flags") {
            repository.emit(greenFlagData)
            repository.emit(greenFlagData)
            repository.emit(yellowFlagData)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(greenFlagJson, first)
            assertEquals(yellowFlagJson, second)
        }
    }

    @Test
    fun `近接情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeProximityRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(repository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/proximity") {
            repository.emit(proximityDataLeft)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"sideBySideLeftVehicleIds":[3],"sideBySideRightVehicleIds":[],""" +
                    """"lateralDistanceLeftMeters":1.5,"lateralDistanceRightMeters":1.7976931348623157E308}""",
                message,
            )
        }
    }

    @Test
    fun `近接情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeProximityRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(repository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/proximity") {
            repository.emit(proximityDataLeft)
            repository.emit(proximityDataLeft)
            repository.emit(proximityDataRight)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(proximityLeftJson, first)
            assertEquals(proximityRightJson, second)
        }
    }

    @Test
    fun `車両故障情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeVehicleDamageRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(repository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/damage") {
            repository.emit(overheatingDamage)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}""",
                message,
            )
        }
    }

    @Test
    fun `車両故障情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeVehicleDamageRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(repository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/damage") {
            repository.emit(overheatingDamage)
            repository.emit(overheatingDamage)
            repository.emit(partDetachedDamage)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(overheatingDamageJson, first)
            assertEquals(partDetachedDamageJson, second)
        }
    }

    @Test
    fun `カーカス温度情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeTyreCarcassTemperatureRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(repository),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
            repository.emit(tyreCarcassTemperatureData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(tyreCarcassTemperatureJson1, message)
        }
    }

    @Test
    fun `カーカス温度情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeTyreCarcassTemperatureRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(repository),
                observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/tyre_carcass_temperature") {
            repository.emit(tyreCarcassTemperatureData1)
            repository.emit(tyreCarcassTemperatureData1)
            repository.emit(tyreCarcassTemperatureData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(tyreCarcassTemperatureJson1, first)
            assertEquals(tyreCarcassTemperatureJson2, second)
        }
    }

    @Test
    fun `KoDriverServerはstartで起動しstopで停止する`() {
        val port = ServerSocket(0).use { it.localPort }
        val server = KoDriverServer(
            observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
            observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
            observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
            observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(EmptyTyreCarcassTemperatureRepository),
            observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
            port = port,
            host = "127.0.0.1",
        )
        server.start()
        try {
            val response = URI("http://127.0.0.1:$port/").toURL().readText()
            assertEquals("Hello, Ktor!", response)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `タイミング情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeLmuWindowsRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(repository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/my_best_lap") {
            repository.emit(timingTelemetryData1)

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(timingJson1, message)
        }
    }

    @Test
    fun `タイミング情報の同一値は重複して送信されない`() = testApplication {
        val repository = FakeLmuWindowsRepository()
        application {
            module(
                observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
                observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
                observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
                observeTyreCarcassTemperature = ObserveTyreCarcassTemperatureUseCase(
                    EmptyTyreCarcassTemperatureRepository,
                ),
                observeLmuWindows = ObserveLmuWindowsUseCase(repository),
            )
        }

        client.config {
            install(WebSockets)
        }.webSocket("/ws/lmu_windows/my_best_lap") {
            repository.emit(timingTelemetryData1)
            repository.emit(timingTelemetryData1)
            repository.emit(timingTelemetryData2)

            val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
            val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

            assertEquals(timingJson1, first)
            assertEquals(timingJson2, second)
        }
    }

    @Test
    fun `createKoDriverServerはKoinから依存を解決してKoDriverServerを生成する`() {
        val koin = startKoin {
            modules(
                module {
                    single<FlagRepository> { FakeFlagRepository() }
                    single<ProximityRepository> { EmptyProximityRepository }
                    single<VehicleDamageRepository> { EmptyVehicleDamageRepository }
                    single<TyreCarcassTemperatureRepository> { EmptyTyreCarcassTemperatureRepository }
                    single<LmuWindowsRepository> { EmptyLmuWindowsRepository }
                },
            )
        }.koin
        try {
            val server = createKoDriverServer(koin)
            assertNotNull(server)
        } finally {
            stopKoin()
        }
    }
}

// --- テストデータ ---

private val greenFlagData = RaceFlagsData(
    gamePhase = SessionPhase.GREEN_FLAG,
    yellowFlagState = SessionYellowFlagState.NONE,
    sectorFlags = listOf(SectorFlagState.CLEAR, SectorFlagState.YELLOW, SectorFlagState.CLEAR),
    startLight = 4,
    numRedLights = 2,
    playerFlag = PrimaryFlag.BLUE,
    playerUnderYellow = true,
    playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
)

private val yellowFlagData = RaceFlagsData(
    gamePhase = SessionPhase.FULL_COURSE_YELLOW,
    yellowFlagState = SessionYellowFlagState.PENDING,
    sectorFlags = listOf(SectorFlagState.YELLOW, SectorFlagState.YELLOW, SectorFlagState.YELLOW),
    startLight = 0,
    numRedLights = 0,
    playerFlag = PrimaryFlag.UNKNOWN,
    playerUnderYellow = true,
    playerCountLapFlag = CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME,
)

private const val greenFlagJson =
    """{"gamePhase":"GREEN_FLAG","yellowFlagState":"NONE","sectorFlags":["CLEAR","YELLOW","CLEAR"],""" +
        """"startLight":4,"numRedLights":2,"playerFlag":"BLUE","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"COUNT_LAP_AND_TIME"}"""

private const val yellowFlagJson =
    """{"gamePhase":"FULL_COURSE_YELLOW","yellowFlagState":"PENDING","sectorFlags":["YELLOW","YELLOW","YELLOW"],""" +
        """"startLight":0,"numRedLights":0,"playerFlag":"UNKNOWN","playerUnderYellow":true,""" +
        """"playerCountLapFlag":"DO_NOT_COUNT_LAP_OR_TIME"}"""

private val proximityDataLeft = ProximityData(
    sideBySideLeftVehicleIds = setOf(3),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = 1.5,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)

private val proximityDataRight = ProximityData(
    sideBySideLeftVehicleIds = emptySet(),
    sideBySideRightVehicleIds = setOf(5),
    lateralDistanceLeftMeters = Double.MAX_VALUE,
    lateralDistanceRightMeters = 2.0,
)

private const val proximityLeftJson =
    """{"sideBySideLeftVehicleIds":[3],"sideBySideRightVehicleIds":[],""" +
        """"lateralDistanceLeftMeters":1.5,"lateralDistanceRightMeters":1.7976931348623157E308}"""

private const val proximityRightJson =
    """{"sideBySideLeftVehicleIds":[],"sideBySideRightVehicleIds":[5],""" +
        """"lateralDistanceLeftMeters":1.7976931348623157E308,"lateralDistanceRightMeters":2.0}"""

private val overheatingDamage = VehicleDamageData(
    overheating = true,
    partDetached = false,
    lastImpactMagnitude = 0.5,
)

private val partDetachedDamage = VehicleDamageData(
    overheating = false,
    partDetached = true,
    lastImpactMagnitude = 1.2,
)

private const val overheatingDamageJson =
    """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}"""

private const val partDetachedDamageJson =
    """{"overheating":false,"partDetached":true,"lastImpactMagnitude":1.2}"""

private val tyreCarcassTemperatureData1 = TyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to 80.0,
        WheelIndex.FRONT_RIGHT to 82.0,
        WheelIndex.REAR_LEFT to 85.0,
        WheelIndex.REAR_RIGHT to 87.0,
    ),
)

private val tyreCarcassTemperatureData2 = TyreCarcassTemperatureData(
    wheels = mapOf(
        WheelIndex.FRONT_LEFT to 90.0,
        WheelIndex.FRONT_RIGHT to 91.0,
        WheelIndex.REAR_LEFT to 92.0,
        WheelIndex.REAR_RIGHT to 93.0,
    ),
)

private const val tyreCarcassTemperatureJson1 =
    """{"wheels":{"FRONT_LEFT":80.0,"FRONT_RIGHT":82.0,"REAR_LEFT":85.0,"REAR_RIGHT":87.0}}"""

private const val tyreCarcassTemperatureJson2 =
    """{"wheels":{"FRONT_LEFT":90.0,"FRONT_RIGHT":91.0,"REAR_LEFT":92.0,"REAR_RIGHT":93.0}}"""

// --- Fake リポジトリ ---

private class FakeFlagRepository : FlagRepository {
    private val channel = Channel<RaceFlagsData>(capacity = Channel.UNLIMITED)

    override fun flagStream(): Flow<RaceFlagsData> = channel.receiveAsFlow()

    fun emit(data: RaceFlagsData) {
        channel.trySend(data).getOrThrow()
    }
}

private class FakeProximityRepository : ProximityRepository {
    private val channel = Channel<ProximityData>(capacity = Channel.UNLIMITED)

    override fun proximityStream(): Flow<ProximityData> = channel.receiveAsFlow()

    fun emit(data: ProximityData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

private object EmptyVehicleDamageRepository : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = emptyFlow()
}

private class FakeVehicleDamageRepository : VehicleDamageRepository {
    private val channel = Channel<VehicleDamageData>(capacity = Channel.UNLIMITED)

    override fun vehicleDamageStream(): Flow<VehicleDamageData> = channel.receiveAsFlow()

    fun emit(data: VehicleDamageData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyTyreCarcassTemperatureRepository : TyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> = emptyFlow()
}

private class FakeTyreCarcassTemperatureRepository : TyreCarcassTemperatureRepository {
    private val channel = Channel<TyreCarcassTemperatureData>(capacity = Channel.UNLIMITED)

    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> = channel.receiveAsFlow()

    fun emit(data: TyreCarcassTemperatureData) {
        channel.trySend(data).getOrThrow()
    }
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private class FakeLmuWindowsRepository : LmuWindowsRepository {
    private val channel = Channel<LmuWindowsTelemetryData>(capacity = Channel.UNLIMITED)

    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = channel.receiveAsFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit

    fun emit(data: LmuWindowsTelemetryData) {
        channel.trySend(data).getOrThrow()
    }
}

private val emptyWheels = WheelIndex.entries.associateWith { TyreWheelData(0.0, 0.0, 0.0, 0.0, 0.0) }

private val timingTelemetryData1 = LmuWindowsTelemetryData(
    timestampMs = 0L,
    engine = EngineData(rpm = 0.0, maxRpm = 0.0, gear = 0),
    inputs = InputsData(throttle = 0.0, brake = 0.0, clutch = 0.0, steering = 0.0),
    tyres = TyreData(wheels = emptyWheels),
    fuel = FuelData(currentLiters = 0.0, capacityLiters = 0.0),
    timing = TimingData(
        currentLapTimeMs = 65_000L,
        lastLapTimeMs = 90_000L,
        bestLapTimeMs = 88_000L,
        sector1Ms = 30_000L,
        sector2Ms = 28_000L,
        currentLap = 3,
        maxLaps = 10,
    ),
    vehicle = VehicleData(
        localVelocityX = 0.0, localVelocityY = 0.0, localVelocityZ = 0.0,
        positionX = 0.0, positionY = 0.0, positionZ = 0.0,
    ),
)

private val timingTelemetryData2 = timingTelemetryData1.copy(
    timing = TimingData(
        currentLapTimeMs = 70_000L,
        lastLapTimeMs = 91_000L,
        bestLapTimeMs = 88_000L,
        sector1Ms = 31_000L,
        sector2Ms = 29_000L,
        currentLap = 4,
        maxLaps = 10,
    ),
)

private const val timingJson1 =
    """{"currentLapTimeMs":65000,"lastLapTimeMs":90000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":30000,"sector2Ms":28000,"currentLap":3,"maxLaps":10}"""

private const val timingJson2 =
    """{"currentLapTimeMs":70000,"lastLapTimeMs":91000,"bestLapTimeMs":88000,""" +
        """"sector1Ms":31000,"sector2Ms":29000,"currentLap":4,"maxLaps":10}"""

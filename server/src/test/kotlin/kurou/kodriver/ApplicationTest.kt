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
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
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
    fun `KoDriverServerはstartで起動しstopで停止する`() {
        val port = ServerSocket(0).use { it.localPort }
        val server = KoDriverServer(
            observeRaceFlags = ObserveRaceFlagsUseCase(FakeFlagRepository()),
            observeProximity = ObserveProximityUseCase(EmptyProximityRepository),
            observeVehicleDamage = ObserveVehicleDamageUseCase(EmptyVehicleDamageRepository),
            port = port,
            host = "127.0.0.1",
        )
        server.start(wait = false)
        try {
            val response = URI("http://127.0.0.1:$port/").toURL().readText()
            assertEquals("Hello, Ktor!", response)
        } finally {
            server.stop()
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

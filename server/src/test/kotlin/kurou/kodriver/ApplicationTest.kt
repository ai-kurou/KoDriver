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
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

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
        }.webSocket("/ws/flags") {
            repository.emit(
                RaceFlagsData(
                    gamePhase = SessionPhase.GREEN_FLAG,
                    yellowFlagState = SessionYellowFlagState.NONE,
                    sectorFlags = listOf(
                        SectorFlagState.CLEAR,
                        SectorFlagState.YELLOW,
                        SectorFlagState.CLEAR,
                    ),
                    startLight = 4,
                    numRedLights = 2,
                    playerFlag = PrimaryFlag.BLUE,
                    playerUnderYellow = true,
                    playerCountLapFlag = CountLapFlag.COUNT_LAP_AND_TIME,
                ),
            )

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
        }.webSocket("/ws/proximity") {
            repository.emit(
                ProximityData(
                    sideBySideLeftVehicleIds = setOf(3),
                    sideBySideRightVehicleIds = emptySet(),
                    lateralDistanceLeftMeters = 1.5,
                    lateralDistanceRightMeters = Double.MAX_VALUE,
                ),
            )

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
        }.webSocket("/ws/damage") {
            repository.emit(
                VehicleDamageData(
                    overheating = true,
                    partDetached = false,
                    lastImpactMagnitude = 0.5,
                ),
            )

            val message = withTimeout(1_000) {
                (incoming.receive() as Frame.Text).readText()
            }
            assertEquals(
                """{"overheating":true,"partDetached":false,"lastImpactMagnitude":0.5}""",
                message,
            )
        }
    }
}

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

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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `ルートはサーバーの応答を返す`() = testApplication {
        application {
            module(ObserveRaceFlagsUseCase(FakeFlagRepository()))
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun `フラッグ情報をJSONでWebSocketへ送信する`() = testApplication {
        val repository = FakeFlagRepository()
        application {
            module(ObserveRaceFlagsUseCase(repository))
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
}

private class FakeFlagRepository : FlagRepository {
    private val channel = Channel<RaceFlagsData>(capacity = Channel.UNLIMITED)

    override fun flagStream(): Flow<RaceFlagsData> = channel.receiveAsFlow()

    fun emit(data: RaceFlagsData) {
        channel.trySend(data).getOrThrow()
    }
}

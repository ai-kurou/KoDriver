package kurou.kodriver

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import io.ktor.server.websocket.WebSockets as ServerWebSockets

class TelemetryWebSocketTest {
    @Test
    fun `Originなしの接続ではdistinct有効時に連続する同一値を一度だけ送信する`() =
        testApplication {
            application {
                install(ServerWebSockets)
                routing {
                    telemetryWebSocket(
                        feature = KoDriverServerFeature.FLAGS,
                        simulator = Simulator.LmuWindows,
                    ) {
                        flowOf(1, 1, 2)
                    }
                }
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/flags") {
                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals("1", first)
                    assertEquals("2", second)
                }
        }

    @Test
    fun `distinct無効時は連続する同一値も送信する`() =
        testApplication {
            application {
                install(ServerWebSockets)
                routing {
                    telemetryWebSocket(
                        feature = KoDriverServerFeature.FLAGS,
                        simulator = Simulator.LmuWindows,
                        distinct = false,
                    ) {
                        flowOf(1, 1)
                    }
                }
            }

            client
                .config {
                    install(WebSockets)
                }.webSocket("/ws/lmu_windows/flags") {
                    val first = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }
                    val second = withTimeout(1_000) { (incoming.receive() as Frame.Text).readText() }

                    assertEquals("1", first)
                    assertEquals("1", second)
                }
        }
}

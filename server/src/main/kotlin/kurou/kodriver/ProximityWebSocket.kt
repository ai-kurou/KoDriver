package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.usecase.ObserveProximityUseCase

private const val PROXIMITY_WEBSOCKET_PATH = "/ws/proximity"

internal fun Route.proximityWebSocket(observeProximity: ObserveProximityUseCase) {
    webSocket(PROXIMITY_WEBSOCKET_PATH) {
        observeProximity()
            .distinctUntilChanged()
            .collect { proximity ->
                send(Frame.Text(proximityJson.encodeToString(proximity)))
            }
    }
}

private val proximityJson = Json {
    encodeDefaults = true
}

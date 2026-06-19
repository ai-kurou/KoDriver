package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveProximityUseCase

internal fun Route.proximityWebSocket(observeProximity: ObserveProximityUseCase) {
    webSocket(KoDriverServerFeature.PROXIMITY.webSocketPath(Simulator.LMU)) {
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

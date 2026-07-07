package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveTyreCarcassTemperatureUseCase

internal fun Route.tyreCarcassTemperatureWebSocket(
    observeTyreCarcassTemperature: ObserveTyreCarcassTemperatureUseCase,
) {
    webSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE.webSocketPath(Simulator.LmuWindows)) {
        observeTyreCarcassTemperature()
            .distinctUntilChanged()
            .collect { data ->
                send(Frame.Text(tyreCarcassTemperatureJson.encodeToString(data)))
            }
    }
}

private val tyreCarcassTemperatureJson = Json {
    encodeDefaults = true
}

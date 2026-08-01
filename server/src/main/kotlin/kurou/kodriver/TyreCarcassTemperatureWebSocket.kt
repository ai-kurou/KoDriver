package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase

internal fun Route.tyreCarcassTemperatureWebSocket(observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase) {
    webSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE.webSocketPath(Simulator.LmuWindows)) {
        observeTyreCarcassTemperature()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

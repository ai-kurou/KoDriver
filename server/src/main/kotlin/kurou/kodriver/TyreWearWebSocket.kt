package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase

internal fun Route.tyreWearWebSocket(observeTyreWear: ObserveLmuWindowsTyreWearUseCase) {
    webSocket(KoDriverServerFeature.TYRE_WEAR.webSocketPath(Simulator.LmuWindows)) {
        observeTyreWear()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

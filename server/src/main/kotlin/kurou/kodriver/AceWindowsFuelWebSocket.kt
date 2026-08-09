package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.core.model.KoDriverServerFeature
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase

internal fun Route.aceWindowsFuelWebSocket(observeAceWindowsFuel: ObserveAceWindowsFuelUseCase) {
    webSocket(KoDriverServerFeature.FUEL.webSocketPath(Simulator.AceWindows)) {
        observeAceWindowsFuel()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

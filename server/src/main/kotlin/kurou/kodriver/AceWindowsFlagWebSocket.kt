package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase

internal fun Route.aceWindowsFlagWebSocket(observeAceWindowsFlag: ObserveAceWindowsFlagUseCase) {
    webSocket(KoDriverServerFeature.FLAGS.webSocketPath(Simulator.AceWindows)) {
        observeAceWindowsFlag()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

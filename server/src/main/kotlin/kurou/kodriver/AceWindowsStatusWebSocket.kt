package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase

internal fun Route.aceWindowsStatusWebSocket(observeAceWindowsStatus: ObserveAceWindowsStatusUseCase) {
    webSocket(KoDriverServerFeature.STATUS.webSocketPath(Simulator.AceWindows)) {
        observeAceWindowsStatus()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

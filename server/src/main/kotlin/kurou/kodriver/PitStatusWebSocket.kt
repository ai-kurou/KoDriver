package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.core.model.KoDriverServerFeature
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase

internal fun Route.pitStatusWebSocket(observePitStatus: ObserveLmuWindowsPitStatusUseCase) {
    webSocket(KoDriverServerFeature.PIT_STATUS.webSocketPath(Simulator.LmuWindows)) {
        observePitStatus()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

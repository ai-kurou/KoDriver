package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase

internal fun Route.lmuWindowsFlagWebSocket(observeLmuWindowsRaceFlags: ObserveLmuWindowsRaceFlagsUseCase) {
    webSocket(KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows)) {
        observeLmuWindowsRaceFlags()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

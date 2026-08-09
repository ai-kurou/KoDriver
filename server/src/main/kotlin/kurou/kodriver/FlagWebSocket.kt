package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.core.model.KoDriverServerFeature
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase

internal fun Route.flagWebSocket(observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase) {
    webSocket(KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows)) {
        observeRaceFlags()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

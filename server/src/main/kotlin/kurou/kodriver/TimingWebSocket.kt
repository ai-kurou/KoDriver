package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase

internal fun Route.timingWebSocket(
    observeLmuWindows: ObserveLmuWindowsUseCase,
) {
    webSocket(KoDriverServerFeature.MY_BEST_LAP.webSocketPath(Simulator.LmuWindows)) {
        observeLmuWindows()
            .map { it.timing }
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

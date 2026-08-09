package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.core.model.KoDriverServerFeature
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase

internal fun Route.virtualEnergyWebSocket(observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase) {
    webSocket(KoDriverServerFeature.VIRTUAL_ENERGY.webSocketPath(Simulator.LmuWindows)) {
        observeVirtualEnergy()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

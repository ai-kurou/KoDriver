package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase

internal fun Route.lmuWindowsVehicleApproachWebSocket(
    observeLmuWindowsVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
) {
    webSocket(KoDriverServerFeature.VEHICLE_APPROACH.webSocketPath(Simulator.LmuWindows)) {
        observeLmuWindowsVehicleApproach()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

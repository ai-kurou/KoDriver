package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.core.model.KoDriverServerFeature
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase

internal fun Route.vehicleClassWebSocket(observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase) {
    webSocket(KoDriverServerFeature.VEHICLE_CLASS.webSocketPath(Simulator.LmuWindows)) {
        observeVehicleClass()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

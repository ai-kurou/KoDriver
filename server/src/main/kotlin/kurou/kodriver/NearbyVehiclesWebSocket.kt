package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsNearbyVehiclesUseCase

internal fun Route.nearbyVehiclesWebSocket(observeNearbyVehicles: ObserveLmuWindowsNearbyVehiclesUseCase) {
    webSocket(KoDriverServerFeature.NEARBY_VEHICLES.webSocketPath(Simulator.LmuWindows)) {
        observeNearbyVehicles()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

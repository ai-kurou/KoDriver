package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase

internal fun Route.vehicleApproachWebSocket(observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase) {
    webSocket(KoDriverServerFeature.VEHICLE_APPROACH.webSocketPath(Simulator.LmuWindows)) {
        observeVehicleApproach()
            .distinctUntilChanged()
            .collect { vehicleApproach ->
                send(Frame.Text(vehicleApproachJson.encodeToString(vehicleApproach)))
            }
    }
}

private val vehicleApproachJson = Json {
    encodeDefaults = true
}

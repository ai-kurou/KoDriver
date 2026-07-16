package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.distinctUntilChanged
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase

internal fun Route.vehicleDamageWebSocket(observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase) {
    webSocket(KoDriverServerFeature.DAMAGE.webSocketPath(Simulator.LmuWindows)) {
        observeVehicleDamage()
            .distinctUntilChanged()
            .let { sendJsonMessages(it) }
    }
}

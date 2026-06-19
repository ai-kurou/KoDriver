package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase

internal fun Route.damageWebSocket(observeVehicleDamage: ObserveVehicleDamageUseCase) {
    webSocket(KoDriverServerFeature.DAMAGE.webSocketPath(Simulator.LMU)) {
        observeVehicleDamage()
            .distinctUntilChanged()
            .collect { damage ->
                send(Frame.Text(damageJson.encodeToString(damage)))
            }
    }
}

private val damageJson = Json {
    encodeDefaults = true
}

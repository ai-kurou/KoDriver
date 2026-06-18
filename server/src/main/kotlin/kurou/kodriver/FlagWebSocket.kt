package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase

private const val FLAG_WEBSOCKET_PATH = "/ws/lmu_windows/flags"

private val json = Json {
    encodeDefaults = true
}

internal fun Route.flagWebSocket(observeRaceFlags: ObserveRaceFlagsUseCase) {
    webSocket(FLAG_WEBSOCKET_PATH) {
        observeRaceFlags()
            .distinctUntilChanged()
            .collect { flags ->
                send(Frame.Text(json.encodeToString(flags)))
            }
    }
}

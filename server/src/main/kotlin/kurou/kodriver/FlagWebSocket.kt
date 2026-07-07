package kurou.kodriver

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase

private val flagsJson = Json {
    encodeDefaults = true
}

internal fun Route.flagWebSocket(observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase) {
    webSocket(KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows)) {
        observeRaceFlags()
            .distinctUntilChanged()
            .collect { flags ->
                send(Frame.Text(flagsJson.encodeToString(flags)))
            }
    }
}

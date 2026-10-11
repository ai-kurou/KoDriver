package kurou.kodriver

import io.ktor.server.routing.Route
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

internal fun Route.aceWindowsRoutes(useCases: KoDriverServerUseCases) {
    telemetryWebSocket(KoDriverServerFeature.FUEL, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsFuel()
    }
    telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsFlag()
    }
    telemetryWebSocket(KoDriverServerFeature.STATUS, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsStatus()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsTyreCarcassTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsVehicleApproach()
    }
    telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsBestLapTime()
    }
    telemetryWebSocket(KoDriverServerFeature.REMAINING_FUEL_LAPS, Simulator.AceWindows) {
        useCases.aceWindows.observeAceWindowsRemainingFuelLaps()
    }
}

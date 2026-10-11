package kurou.kodriver

import io.ktor.server.routing.Route
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

internal fun Route.aceWindowsRoutes(useCases: KoDriverServerUseCases) {
    telemetryWebSocket(KoDriverServerFeature.FUEL, Simulator.AceWindows) {
        useCases.observeAceWindowsFuel()
    }
    telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.AceWindows) {
        useCases.observeAceWindowsFlag()
    }
    telemetryWebSocket(KoDriverServerFeature.STATUS, Simulator.AceWindows) {
        useCases.observeAceWindowsStatus()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.AceWindows) {
        useCases.observeAceWindowsTyreCarcassTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.AceWindows) {
        useCases.observeAceWindowsVehicleApproach()
    }
    telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.AceWindows) {
        useCases.observeAceWindowsBestLapTime()
    }
    telemetryWebSocket(KoDriverServerFeature.REMAINING_FUEL_LAPS, Simulator.AceWindows) {
        useCases.observeAceWindowsRemainingFuelLaps()
    }
}

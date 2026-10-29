package kurou.kodriver

import io.ktor.server.routing.Route
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

internal fun Route.aceWindowsRoutes(useCases: KoDriverServerUseCases) {
    telemetryWebSocket(KoDriverServerFeature.FUEL, Simulator.AceWindows, distinct = false) {
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
    telemetryWebSocket(KoDriverServerFeature.REMAINING_FUEL_LAPS, Simulator.AceWindows, distinct = false) {
        useCases.aceWindows.observeAceWindowsRemainingFuelLaps()
    }
    telemetryWebSocket(KoDriverServerFeature.CAR_GAP, Simulator.AceWindows, distinct = false) {
        useCases.aceWindows.observeAceWindowsGap()
    }
    telemetryWebSocket(KoDriverServerFeature.OVERALL_POSITION, Simulator.AceWindows, distinct = false) {
        useCases.aceWindows.observeAceWindowsOverallPosition()
    }
}

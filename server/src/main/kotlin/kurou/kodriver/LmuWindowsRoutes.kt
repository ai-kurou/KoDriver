package kurou.kodriver

import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

internal fun Route.lmuWindowsRoutes(useCases: KoDriverServerUseCases) {
    telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.LmuWindows) {
        useCases.observeLmuWindowsRaceFlags()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.LmuWindows) {
        useCases.observeLmuWindowsVehicleApproach()
    }
    telemetryWebSocket(KoDriverServerFeature.DAMAGE, Simulator.LmuWindows) {
        useCases.observeLmuWindowsVehicleDamage()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.LmuWindows) {
        useCases.observeLmuWindowsTyreCarcassTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.BRAKE_TEMPERATURE, Simulator.LmuWindows) {
        useCases.observeLmuWindowsBrakeTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_CLASS, Simulator.LmuWindows) {
        useCases.observeLmuWindowsVehicleClass()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_WEAR, Simulator.LmuWindows) {
        useCases.observeLmuWindowsTyreWear()
    }
    telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.LmuWindows) {
        useCases.observeLmuWindows().map { it.timing }
    }
    telemetryWebSocket(KoDriverServerFeature.VIRTUAL_ENERGY, Simulator.LmuWindows) {
        useCases.observeLmuWindowsVirtualEnergy()
    }
    telemetryWebSocket(KoDriverServerFeature.PIT_STATUS, Simulator.LmuWindows) {
        useCases.observeLmuWindowsPitStatus()
    }
    telemetryWebSocket(KoDriverServerFeature.BRAKE_WEAR, Simulator.LmuWindows, distinct = false) {
        useCases.observeLmuWindowsBrakeWear()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_DETACHED, Simulator.LmuWindows) {
        useCases.observeLmuWindowsTyreDetached()
    }
}

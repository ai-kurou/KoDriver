package kurou.kodriver

import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.KoDriverServerFeature
import kurou.kodriver.domain.model.Simulator

internal fun Route.lmuWindowsRoutes(useCases: KoDriverServerUseCases) {
    telemetryWebSocket(KoDriverServerFeature.FLAGS, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsRaceFlags()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_APPROACH, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsVehicleApproach()
    }
    telemetryWebSocket(KoDriverServerFeature.DAMAGE, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsVehicleDamage()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsTyreCarcassTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.BRAKE_TEMPERATURE, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsBrakeTemperature()
    }
    telemetryWebSocket(KoDriverServerFeature.VEHICLE_CLASS, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsVehicleClass()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_WEAR, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsTyreWear()
    }
    telemetryWebSocket(KoDriverServerFeature.MY_BEST_LAP, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindows().map { it.timing }
    }
    telemetryWebSocket(KoDriverServerFeature.VIRTUAL_ENERGY, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsVirtualEnergy()
    }
    telemetryWebSocket(KoDriverServerFeature.PIT_STATUS, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsPitStatus()
    }
    telemetryWebSocket(KoDriverServerFeature.BRAKE_WEAR, Simulator.LmuWindows, distinct = false) {
        useCases.lmuWindows.observeLmuWindowsBrakeWear()
    }
    telemetryWebSocket(KoDriverServerFeature.TYRE_DETACHED, Simulator.LmuWindows) {
        useCases.lmuWindows.observeLmuWindowsTyreDetached()
    }
}

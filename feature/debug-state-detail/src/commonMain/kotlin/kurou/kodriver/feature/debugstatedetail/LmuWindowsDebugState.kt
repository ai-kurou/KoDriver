package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData

data class LmuWindowsDebugState(
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val telemetry: LmuWindowsTelemetryData? = null,
    val pitStatus: LmuWindowsPitStatusData? = null,
    val sideBySideDurations: LmuWindowsSideBySideDurations? = null,
    val vehicleApproach: LmuWindowsVehicleApproachData? = null,
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData? = null,
    val brakeWear: LmuWindowsBrakeWearRemainingData? = null,
    val brakeTemperature: LmuWindowsBrakeTemperatureData? = null,
    val vehicleClass: LmuWindowsVehicleClassData? = null,
    val vehicleDamage: LmuWindowsVehicleDamageData? = null,
    val tyreDetached: LmuWindowsTyreDetachedData? = null,
)

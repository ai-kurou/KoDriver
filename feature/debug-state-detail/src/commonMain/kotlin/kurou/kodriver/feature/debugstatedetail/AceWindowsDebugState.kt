package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.DebugStateCardKey

data class AceWindowsDebugState(
    val fuel: AceWindowsFuelData? = null,
    val flag: AceWindowsFlagData? = null,
    val status: AceWindowsStatusData? = null,
    val bestLapTime: AceWindowsBestLapTimeData? = null,
    val remainingFuelLaps: AceWindowsRemainingFuelLapsData? = null,
    val vehicleApproach: AceWindowsVehicleApproachData? = null,
    val tyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData? = null,
)

internal val aceWindowsSupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_LOCATION,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        DebugStateCardKey.BEST_LAP,
    )

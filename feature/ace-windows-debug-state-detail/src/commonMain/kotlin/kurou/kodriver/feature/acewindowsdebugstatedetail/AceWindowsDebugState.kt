package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData

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
        AceWindowsDebugStateCardKey.SIMULATOR,
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION,
        AceWindowsDebugStateCardKey.FLAG_INFO,
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION,
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        AceWindowsDebugStateCardKey.BEST_LAP,
    )

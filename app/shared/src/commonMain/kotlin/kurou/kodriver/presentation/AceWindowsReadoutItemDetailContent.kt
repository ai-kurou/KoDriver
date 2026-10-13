package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.acewindowsreadout.flagdetail.AceWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.AceWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.acewindowsreadout.remainingfueldetail.AceWindowsReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.AceWindowsReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.AceWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.AceWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.autoreadoutlist.AceWindowsAutoReadoutListItemType

@Composable
internal fun AceWindowsReadoutItemDetailContent(itemType: AceWindowsAutoReadoutListItemType) {
    when (itemType) {
        AceWindowsAutoReadoutListItemType.Flag -> {
            AceWindowsReadoutFlagDetailPane()
        }

        AceWindowsAutoReadoutListItemType.TyreTemperature -> {
            AceWindowsReadoutTyreTemperatureDetailPane()
        }

        AceWindowsAutoReadoutListItemType.RemainingFuel -> {
            AceWindowsReadoutRemainingFuelDetailPane()
        }

        AceWindowsAutoReadoutListItemType.RemainingFuelLaps -> {
            AceWindowsReadoutRemainingFuelLapsDetailPane()
        }

        AceWindowsAutoReadoutListItemType.VehicleApproach -> {
            AceWindowsReadoutVehicleApproachDetailPane()
        }

        AceWindowsAutoReadoutListItemType.MyBestLap -> {
            AceWindowsReadoutMyBestLapDetailPane()
        }
    }
}

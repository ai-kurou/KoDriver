package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.acewindowsreadout.flagdetail.AceWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.AceWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.acewindowsreadout.remainingfueldetail.AceWindowsReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.AceWindowsReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.AceWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.AceWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.readoutlist.AceWindowsReadoutListItemType

@Composable
internal fun AceWindowsReadoutItemDetailContent(itemType: AceWindowsReadoutListItemType) {
    when (itemType) {
        AceWindowsReadoutListItemType.Flag -> {
            AceWindowsReadoutFlagDetailPane()
        }

        AceWindowsReadoutListItemType.TyreTemperature -> {
            AceWindowsReadoutTyreTemperatureDetailPane()
        }

        AceWindowsReadoutListItemType.RemainingFuel -> {
            AceWindowsReadoutRemainingFuelDetailPane()
        }

        AceWindowsReadoutListItemType.RemainingFuelLaps -> {
            AceWindowsReadoutRemainingFuelLapsDetailPane()
        }

        AceWindowsReadoutListItemType.VehicleApproach -> {
            AceWindowsReadoutVehicleApproachDetailPane()
        }

        AceWindowsReadoutListItemType.MyBestLap -> {
            AceWindowsReadoutMyBestLapDetailPane()
        }
    }
}

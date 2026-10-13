package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.AceWindowsAutoReadoutFlagDetailPane
import kurou.kodriver.feature.acewindowsautoreadout.mybestlapdetail.AceWindowsAutoReadoutMyBestLapDetailPane
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.AceWindowsAutoReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.AceWindowsAutoReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.acewindowsautoreadout.tyretemperaturedetail.AceWindowsAutoReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.AceWindowsAutoReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.autoreadoutlist.AceWindowsAutoReadoutListItemType

@Composable
internal fun AceWindowsAutoReadoutItemDetailContent(itemType: AceWindowsAutoReadoutListItemType) {
    when (itemType) {
        AceWindowsAutoReadoutListItemType.Flag -> {
            AceWindowsAutoReadoutFlagDetailPane()
        }

        AceWindowsAutoReadoutListItemType.TyreTemperature -> {
            AceWindowsAutoReadoutTyreTemperatureDetailPane()
        }

        AceWindowsAutoReadoutListItemType.RemainingFuel -> {
            AceWindowsAutoReadoutRemainingFuelDetailPane()
        }

        AceWindowsAutoReadoutListItemType.RemainingFuelLaps -> {
            AceWindowsAutoReadoutRemainingFuelLapsDetailPane()
        }

        AceWindowsAutoReadoutListItemType.VehicleApproach -> {
            AceWindowsAutoReadoutVehicleApproachDetailPane()
        }

        AceWindowsAutoReadoutListItemType.MyBestLap -> {
            AceWindowsAutoReadoutMyBestLapDetailPane()
        }
    }
}

package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.autoreadoutlist.LmuWindowsAutoReadoutListItemType
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.LmuWindowsAutoReadoutBrakeTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.LmuWindowsAutoReadoutBrakeWearDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail.LmuWindowsAutoReadoutFlagDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.LmuWindowsAutoReadoutMyBestLapDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail.LmuWindowsAutoReadoutPitTimingDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.LmuWindowsAutoReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail.LmuWindowsAutoReadoutRemainingVirtualEnergyDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.LmuWindowsAutoReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.tyreweardetail.LmuWindowsAutoReadoutTyreWearDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail.LmuWindowsAutoReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail.LmuWindowsAutoReadoutVehicleDamageDetailPane

@Composable
internal fun LmuWindowsAutoReadoutItemDetailContent(itemType: LmuWindowsAutoReadoutListItemType) {
    when (itemType) {
        LmuWindowsAutoReadoutListItemType.VehicleApproach -> {
            LmuWindowsAutoReadoutVehicleApproachDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.Flag -> {
            LmuWindowsAutoReadoutFlagDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.VehicleDamage -> {
            LmuWindowsAutoReadoutVehicleDamageDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.TyreTemperature -> {
            LmuWindowsAutoReadoutTyreTemperatureDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.PitTiming -> {
            LmuWindowsAutoReadoutPitTimingDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.RemainingVirtualEnergy -> {
            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.RemainingFuel -> {
            LmuWindowsAutoReadoutRemainingFuelDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.TyreWear -> {
            LmuWindowsAutoReadoutTyreWearDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.BrakeTemperature -> {
            LmuWindowsAutoReadoutBrakeTemperatureDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.BrakeWear -> {
            LmuWindowsAutoReadoutBrakeWearDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.MyBestLap -> {
            LmuWindowsAutoReadoutMyBestLapDetailPane()
        }
    }
}

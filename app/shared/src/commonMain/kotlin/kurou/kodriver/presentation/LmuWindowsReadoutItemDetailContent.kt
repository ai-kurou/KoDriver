package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.LmuWindowsReadoutBrakeTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.LmuWindowsReadoutBrakeWearDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.LmuWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.LmuWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.LmuWindowsReadoutPitTimingDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.LmuWindowsReadoutRemainingVirtualEnergyDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.LmuWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail.LmuWindowsReadoutTyreWearDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.LmuWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.LmuWindowsReadoutVehicleDamageDetailPane
import kurou.kodriver.feature.readoutlist.LmuWindowsReadoutListItemType

@Composable
internal fun LmuWindowsReadoutItemDetailContent(itemType: LmuWindowsReadoutListItemType) {
    when (itemType) {
        LmuWindowsReadoutListItemType.VehicleApproach -> {
            LmuWindowsReadoutVehicleApproachDetailPane()
        }

        LmuWindowsReadoutListItemType.Flag -> {
            LmuWindowsReadoutFlagDetailPane()
        }

        LmuWindowsReadoutListItemType.VehicleDamage -> {
            LmuWindowsReadoutVehicleDamageDetailPane()
        }

        LmuWindowsReadoutListItemType.TyreTemperature -> {
            LmuWindowsReadoutTyreTemperatureDetailPane()
        }

        LmuWindowsReadoutListItemType.PitTiming -> {
            LmuWindowsReadoutPitTimingDetailPane()
        }

        LmuWindowsReadoutListItemType.RemainingVirtualEnergy -> {
            LmuWindowsReadoutRemainingVirtualEnergyDetailPane()
        }

        LmuWindowsReadoutListItemType.TyreWear -> {
            LmuWindowsReadoutTyreWearDetailPane()
        }

        LmuWindowsReadoutListItemType.BrakeTemperature -> {
            LmuWindowsReadoutBrakeTemperatureDetailPane()
        }

        LmuWindowsReadoutListItemType.BrakeWear -> {
            LmuWindowsReadoutBrakeWearDetailPane()
        }

        LmuWindowsReadoutListItemType.MyBestLap -> {
            LmuWindowsReadoutMyBestLapDetailPane()
        }
    }
}

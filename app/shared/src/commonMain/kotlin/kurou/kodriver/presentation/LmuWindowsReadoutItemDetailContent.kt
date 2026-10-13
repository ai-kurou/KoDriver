package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.autoreadoutlist.LmuWindowsAutoReadoutListItemType
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

@Composable
internal fun LmuWindowsReadoutItemDetailContent(itemType: LmuWindowsAutoReadoutListItemType) {
    when (itemType) {
        LmuWindowsAutoReadoutListItemType.VehicleApproach -> {
            LmuWindowsReadoutVehicleApproachDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.Flag -> {
            LmuWindowsReadoutFlagDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.VehicleDamage -> {
            LmuWindowsReadoutVehicleDamageDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.TyreTemperature -> {
            LmuWindowsReadoutTyreTemperatureDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.PitTiming -> {
            LmuWindowsReadoutPitTimingDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.RemainingVirtualEnergy -> {
            LmuWindowsReadoutRemainingVirtualEnergyDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.TyreWear -> {
            LmuWindowsReadoutTyreWearDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.BrakeTemperature -> {
            LmuWindowsReadoutBrakeTemperatureDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.BrakeWear -> {
            LmuWindowsReadoutBrakeWearDetailPane()
        }

        LmuWindowsAutoReadoutListItemType.MyBestLap -> {
            LmuWindowsReadoutMyBestLapDetailPane()
        }
    }
}

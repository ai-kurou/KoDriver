package kurou.kodriver.presentation

import kurou.kodriver.feature.lmuwindowsconnection.lmuConnectionModule
import kurou.kodriver.feature.lmuwindowsnarrator.lmuNarratorModule
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.lmuWindowsReadoutFlagDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.lmuReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.lmuWindowsReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.otherserveripdetail.otherServerIpDetailModule
import kurou.kodriver.feature.othervolumedetail.otherVolumeDetailModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import kurou.kodriver.feature.serverconnection.serverConnectionModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    lmuConnectionModule,
    serverConnectionModule,
    lmuNarratorModule,
    otherListModule,
    otherServerIpDetailModule,
    otherVolumeDetailModule,
    readoutListModule,
    lmuReadoutVehicleApproachDetailModule,
    lmuWindowsReadoutFlagDetailModule,
    lmuWindowsReadoutVehicleDamageDetailModule,
)

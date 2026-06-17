package kurou.kodriver.presentation

import kurou.kodriver.feature.lmureadout.flagdetail.lmuReadoutFlagDetailModule
import kurou.kodriver.feature.lmureadout.vehicleapproachdetail.lmuReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmureadout.vehicledamagedetail.lmuReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.lmuwindowsconnection.lmuConnectionModule
import kurou.kodriver.feature.lmuwindowsnarrator.lmuNarratorModule
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
    lmuReadoutFlagDetailModule,
    lmuReadoutVehicleDamageDetailModule,
)

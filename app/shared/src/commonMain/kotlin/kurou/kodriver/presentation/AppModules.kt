package kurou.kodriver.presentation

import kurou.kodriver.feature.gt7ps5connection.gt7Ps5ConnectionModule
import kurou.kodriver.feature.gt7ps5narrator.gt7Ps5NarratorModule
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.gt7Ps5ReadoutMyBestLapDetailModule
import kurou.kodriver.feature.lmuwindowsconnection.lmuConnectionModule
import kurou.kodriver.feature.lmuwindowsnarrator.lmuNarratorModule
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.lmuWindowsReadoutFlagDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.lmuReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.lmuWindowsReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.main.mainModule
import kurou.kodriver.feature.main.mainPlatformModule
import kurou.kodriver.feature.otherconsoleipdetail.otherConsoleIpDetailModule
import kurou.kodriver.feature.otherkeepscreenondetail.otherKeepScreenOnDetailModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.otherreadoutstartsounddetail.otherReadoutStartSoundDetailModule
import kurou.kodriver.feature.otherserveripdetail.otherServerIpDetailModule
import kurou.kodriver.feature.othervolumedetail.otherVolumeDetailModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import kurou.kodriver.feature.serverconnection.serverConnectionModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    mainModule,
    mainPlatformModule,
    lmuConnectionModule,
    gt7Ps5ConnectionModule,
    serverConnectionModule,
    lmuNarratorModule,
    gt7Ps5NarratorModule,
    otherListModule,
    otherReadoutStartSoundDetailModule,
    otherKeepScreenOnDetailModule,
    otherServerIpDetailModule,
    otherConsoleIpDetailModule,
    otherVolumeDetailModule,
    readoutListModule,
    lmuReadoutVehicleApproachDetailModule,
    lmuWindowsReadoutFlagDetailModule,
    lmuWindowsReadoutVehicleDamageDetailModule,
    gt7Ps5ReadoutMyBestLapDetailModule,
)

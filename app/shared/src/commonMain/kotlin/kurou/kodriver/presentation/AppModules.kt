package kurou.kodriver.presentation

import kurou.kodriver.feature.lmuconnection.lmuConnectionModule
import kurou.kodriver.feature.lmunarrator.lmuNarratorModule
import kurou.kodriver.feature.lmureadout.flagdetail.lmuReadoutFlagDetailModule
import kurou.kodriver.feature.lmureadout.vehicleapproachdetail.lmuReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmureadout.vehicledamagedetail.lmuReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    lmuConnectionModule,
    lmuNarratorModule,
    otherListModule,
    readoutListModule,
    lmuReadoutVehicleApproachDetailModule,
    lmuReadoutFlagDetailModule,
    lmuReadoutVehicleDamageDetailModule,
)

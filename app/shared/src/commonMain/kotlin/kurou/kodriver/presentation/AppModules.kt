package kurou.kodriver.presentation

import kurou.kodriver.feature.lmuconnection.lmuConnectionModule
import kurou.kodriver.feature.lmureadout.vehicledamagedetail.lmuReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.narrator.narratorModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.readout.flagdetail.lmuReadoutFlagDetailModule
import kurou.kodriver.feature.readout.readoutModule
import kurou.kodriver.feature.readout.vehicleapproach.vehicleApproachModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    lmuConnectionModule,
    narratorModule,
    otherListModule,
    readoutModule,
    vehicleApproachModule,
    lmuReadoutFlagDetailModule,
    lmuReadoutVehicleDamageDetailModule,
)

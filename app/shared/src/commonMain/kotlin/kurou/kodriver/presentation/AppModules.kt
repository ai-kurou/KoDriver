package kurou.kodriver.presentation

import kurou.kodriver.feature.lmuconnection.lmuConnectionModule
import kurou.kodriver.feature.narrator.narratorModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.readout.flagdetail.flagModule
import kurou.kodriver.feature.readout.readoutModule
import kurou.kodriver.feature.readout.vehicleapproach.vehicleApproachModule
import kurou.kodriver.feature.readout.vehicledamage.vehicleDamageModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    lmuConnectionModule,
    narratorModule,
    otherListModule,
    readoutModule,
    vehicleApproachModule,
    flagModule,
    vehicleDamageModule,
)

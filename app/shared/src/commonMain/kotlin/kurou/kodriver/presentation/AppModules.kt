package kurou.kodriver.presentation

import kurou.kodriver.feature.lmuconnection.lmuConnectionModule
import kurou.kodriver.feature.narrator.narratorModule
import kurou.kodriver.feature.other.otherModule
import kurou.kodriver.feature.readout.flag.flagModule
import kurou.kodriver.feature.readout.readoutModule
import kurou.kodriver.feature.readout.vehicleapproach.vehicleApproachModule
import kurou.kodriver.feature.readout.vehicledamage.vehicleDamageModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    lmuConnectionModule,
    narratorModule,
    otherModule,
    readoutModule,
    vehicleApproachModule,
    flagModule,
    vehicleDamageModule,
)

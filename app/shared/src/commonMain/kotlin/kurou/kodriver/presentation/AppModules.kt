package kurou.kodriver.presentation

import kurou.kodriver.feature.readout.readoutModule
import kurou.kodriver.feature.readout.vehicleapproach.vehicleApproachModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(readoutModule, vehicleApproachModule)

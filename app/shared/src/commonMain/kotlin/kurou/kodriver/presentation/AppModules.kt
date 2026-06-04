package kurou.kodriver.presentation

import kurou.kodriver.feature.readout.readoutModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(readoutModule)

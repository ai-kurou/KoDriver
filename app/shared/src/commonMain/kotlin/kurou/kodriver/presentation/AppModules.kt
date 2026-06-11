package kurou.kodriver.presentation

import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import kurou.kodriver.feature.narrator.narratorModule
import kurou.kodriver.feature.other.otherModule
import kurou.kodriver.feature.readout.flag.flagModule
import kurou.kodriver.feature.readout.readoutModule
import kurou.kodriver.feature.readout.vehicleapproach.vehicleApproachModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val connectionModule = module {
    factory { CheckLmuConnectionUseCase(get()) }
    viewModelOf(::LmuConnectionViewModel)
}

val appModules: List<Module> = listOf(
    connectionModule,
    narratorModule,
    otherModule,
    readoutModule,
    vehicleApproachModule,
    flagModule,
)

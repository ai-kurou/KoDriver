package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gt7Ps5ReadoutMyBestLapDetailModule = module {
    viewModelOf(::Gt7Ps5ReadoutMyBestLapDetailViewModel)
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { SaveReadoutEnabledStateUseCase(get()) }
}

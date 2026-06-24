package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveMyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5ReadoutMyBestLapDetailModule = module {
    viewModel { Gt7Ps5ReadoutMyBestLapDetailViewModel(get(), get(), get(named("gt7_ps5"))) }
    factory { ObserveMyBestLapVoiceTypeUseCase(get()) }
    factory { SaveMyBestLapVoiceTypeUseCase(get()) }
}

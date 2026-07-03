package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val gt7Ps5ReadoutMyBestLapDetailModule = module {
    viewModel { Gt7Ps5ReadoutMyBestLapDetailViewModel(get(), get(), get(named("gt7_ps5"))) }
    factory { ObserveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
    factory { SaveGt7Ps5MyBestLapVoiceTypeUseCase(get()) }
}

package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveMyBestLapVoiceTypeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val gt7Ps5ReadoutMyBestLapDetailModule = module {
    viewModelOf(::Gt7Ps5ReadoutMyBestLapDetailViewModel)
    factory { ObserveMyBestLapVoiceTypeUseCase(get()) }
    factory { SaveMyBestLapVoiceTypeUseCase(get()) }
}

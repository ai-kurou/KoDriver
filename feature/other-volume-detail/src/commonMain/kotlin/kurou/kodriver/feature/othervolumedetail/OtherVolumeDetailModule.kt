package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherVolumeDetailModule = module {
    viewModelOf(::OtherVolumeDetailViewModel)
    factory { ObserveSoundVolumeUseCase(get()) }
    factory { SaveSoundVolumeUseCase(get()) }
}

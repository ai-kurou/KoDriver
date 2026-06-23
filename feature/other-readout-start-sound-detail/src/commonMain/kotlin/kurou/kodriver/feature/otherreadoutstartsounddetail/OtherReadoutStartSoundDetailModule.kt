package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val otherReadoutStartSoundDetailModule = module {
    viewModelOf(::OtherReadoutStartSoundDetailViewModel)
    factory { ObserveReadoutStartSoundTypeUseCase(get()) }
    factory { PreviewStartSoundUseCase(get(named("lmu_windows"))) }
    factory { SaveReadoutStartSoundTypeUseCase(get()) }
}

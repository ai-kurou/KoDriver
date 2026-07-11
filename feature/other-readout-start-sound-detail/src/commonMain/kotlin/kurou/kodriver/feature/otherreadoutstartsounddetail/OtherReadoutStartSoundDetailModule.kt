package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val otherReadoutStartSoundDetailModule = module {
    viewModelOf(::OtherReadoutStartSoundDetailViewModel)
    factory { PreviewStartSoundUseCase(get(named("lmu_windows"))) }
}

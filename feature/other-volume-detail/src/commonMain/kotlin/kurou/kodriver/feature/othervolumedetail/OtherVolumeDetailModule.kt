package kurou.kodriver.feature.othervolumedetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherVolumeDetailModule = module {
    viewModelOf(::OtherVolumeDetailViewModel)
}

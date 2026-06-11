package kurou.kodriver.feature.lmuconnection

import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lmuConnectionModule = module {
    factory { CheckLmuConnectionUseCase(get()) }
    viewModelOf(::LmuConnectionViewModel)
}

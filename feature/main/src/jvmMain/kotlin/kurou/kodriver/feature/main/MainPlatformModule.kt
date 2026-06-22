package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val mainPlatformModule: Module = module {
    factory { CheckLmuWindowsConnectionUseCase(get()) }
    factory<LmuBannerConnectionChecker> { LmuWindowsBannerConnectionChecker(get()) }
}

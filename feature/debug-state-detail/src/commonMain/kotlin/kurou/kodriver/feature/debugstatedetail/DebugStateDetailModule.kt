package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * デバッグ状態画面（debug-state-detail feature）の Koin モジュール。
 *
 * 提供: DebugStateDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsFlagRepository（:core:lmu-windows-data / :core:data）。
 */
val debugStateDetailModule = module {
    viewModel { DebugStateDetailViewModel(get()) }

    factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
}

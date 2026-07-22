package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * デバッグ状態画面（debug-state-detail feature）の Koin モジュール。
 *
 * 提供: DebugStateDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsFlagRepository・SimulatorPreferencesRepository・
 * LmuWindowsVirtualEnergyRepository（:core:lmu-windows-data / :core:data）。
 */
val debugStateDetailModule = module {
    viewModel { DebugStateDetailViewModel(get(), get(), get()) }

    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
    factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
}

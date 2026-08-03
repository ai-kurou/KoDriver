package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * デバッグ状態画面（debug-state-detail feature）の Koin モジュール。
 *
 * 提供: DebugStateDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsFlagRepository・SimulatorPreferencesRepository・
 * LmuWindowsVirtualEnergyRepository・LmuWindowsRepository・Gt7Ps5Repository・AceWindowsFuelRepository・
 * AceWindowsFlagRepository・LmuWindowsVehicleApproachRepository・LmuWindowsTyreCarcassTemperatureRepository・
 * DebugStateCardOrderPreferencesRepository
 * （:core:lmu-windows-data / :core:gt7-ps5-data / :core:ace-windows-data / :core:data）。
 */
val debugStateDetailModule =
    module {
        viewModel {
            DebugStateDetailViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
        factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveAceWindowsFuelUseCase(get()) }
        factory { ObserveAceWindowsFlagUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
        factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveDebugStateCardOrderUseCase(get()) }
        factory { ResolveDebugStateCardOrderUseCase() }
        factory { SaveDebugStateCardOrderUseCase(get()) }
    }

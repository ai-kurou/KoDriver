package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * デバッグ状態画面（debug-state-detail feature）の Koin モジュール。
 *
 * 提供: DebugStateDetailViewModel、この feature 内で定義した UseCase 集約 data class
 *   （LmuWindowsDebugStateUseCases / Gt7Ps5DebugStateUseCases / AceWindowsDebugStateUseCases /
 *   DebugStateCardOrderUseCases）、それらが束ねる各ドメイン UseCase。
 * 消費（get で解決）: LmuWindowsFlagRepository・SimulatorPreferencesRepository・
 * LmuWindowsVirtualEnergyRepository・LmuWindowsRepository・Gt7Ps5Repository・AceWindowsFuelRepository・
 * AceWindowsFlagRepository・LmuWindowsVehicleApproachRepository・LmuWindowsTyreCarcassTemperatureRepository・
 * LmuWindowsVehicleClassRepository・AceWindowsStatusRepository・AceWindowsTyreCarcassTemperatureRepository・
 * AceWindowsVehicleApproachRepository・AceWindowsBestLapTimeRepository・LmuWindowsPitStatusRepository・
 * LmuWindowsVehicleDamageRepository・DebugStateCardOrderPreferencesRepository
 * （:core:lmu-windows-data / :core:gt7-ps5-data / :core:ace-windows-data / :core:data）。
 */
val debugStateDetailModule =
    module {
        viewModel { DebugStateDetailViewModel(get(), get(), get(), get(), get()) }

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { LmuWindowsDebugStateUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { Gt7Ps5DebugStateUseCases(get(), get()) }
        factory { AceWindowsDebugStateUseCases(get(), get(), get(), get(), get(), get()) }
        factory { DebugStateCardOrderUseCases(get(), get(), get()) }

        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
        factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveGt7Ps5VehicleClassUseCase(get()) }
        factory { ObserveAceWindowsFuelUseCase(get()) }
        factory { ObserveAceWindowsFlagUseCase(get()) }
        factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
        factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveLmuWindowsVehicleClassUseCase(get()) }
        factory { ObserveAceWindowsStatusUseCase(get()) }
        factory { ObserveAceWindowsTyreCarcassTemperatureUseCase(get()) }
        factory { ObserveAceWindowsVehicleApproachUseCase(get()) }
        factory { ObserveAceWindowsBestLapTimeUseCase(get()) }
        factory { ObserveLmuWindowsPitStatusUseCase(get()) }
        factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
        factory { ObserveDebugStateCardOrderUseCase(get()) }
        factory { ResolveDebugStateCardOrderUseCase() }
        factory { SaveDebugStateCardOrderUseCase(get()) }
    }

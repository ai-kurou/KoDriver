package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * デバッグ状態画面（debug-state-detail feature）の Koin モジュール。
 *
 * ViewModel と共通の UseCase を登録し、シミュレーター別の UseCase 定義を集約する。
 * 共通の UseCase は SimulatorPreferencesRepository と DebugStateCardOrderPreferencesRepository
 * （:core:data）を get() で解決する。
 */
val debugStateDetailModule =
    module {
        viewModel { DebugStateDetailViewModel(get(), get(), get(), get(), get()) }

        factory { DebugStateCardOrderUseCases(get(), get(), get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveDebugStateCardOrderUseCase(get()) }
        factory { ResolveDebugStateCardOrderUseCase() }
        factory { SaveDebugStateCardOrderUseCase(get()) }

        lmuWindowsDebugStateDefinitions()
        aceWindowsDebugStateDefinitions()
        gt7Ps5DebugStateDefinitions()
    }

package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** LMU 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で登録する。 */
val lmuWindowsDebugStateDetailModule =
    module {
        viewModel { LmuWindowsDebugStateDetailViewModel(get(), get()) }

        factory { DebugStateCardOrderUseCases(get(), get(), get()) }
        factory { ObserveDebugStateCardOrderUseCase(get()) }
        factory { ResolveDebugStateCardOrderUseCase() }
        factory { SaveDebugStateCardOrderUseCase(get()) }

        lmuWindowsDebugStateDefinitions()
    }

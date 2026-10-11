package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** ACE 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で `named(Simulator.AceWindows.id)` として登録する。 */
val aceWindowsDebugStateDetailModule =
    module {
        viewModel { AceWindowsDebugStateDetailViewModel(get(), get()) }

        factory { DebugStateCardOrderUseCases(get(), get(), get()) }
        factory { ObserveDebugStateCardOrderUseCase(get(named(Simulator.AceWindows.id))) }
        factory { ResolveDebugStateCardOrderUseCase() }
        factory { SaveDebugStateCardOrderUseCase(get(named(Simulator.AceWindows.id))) }

        aceWindowsDebugStateDefinitions()
    }

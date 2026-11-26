package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.debug.usecase.DebugStateCardOrderUseCases
import kurou.kodriver.domain.debug.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.debug.usecase.RequestDebugReadoutUseCase
import kurou.kodriver.domain.debug.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.debug.usecase.SaveDebugStateCardOrderUseCase
import kurou.kodriver.domain.model.Simulator
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** LMU 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で `named(Simulator.LmuWindows.id)` として登録する。 */
public val lmuWindowsDebugStateDetailModule: Module =
    module {
        factory { RequestDebugReadoutUseCase(get()) }
        viewModel { LmuWindowsDebugStateDetailViewModel(get(), get(named(Simulator.LmuWindows.id)), get()) }

        factory(named(Simulator.LmuWindows.id)) {
            DebugStateCardOrderUseCases(
                LmuWindowsDebugStateCardKey.entries,
                ObserveDebugStateCardOrderUseCase(get(named(Simulator.LmuWindows.id))),
                ResolveDebugStateCardOrderUseCase(),
                SaveDebugStateCardOrderUseCase(get(named(Simulator.LmuWindows.id))),
            )
        }

        lmuWindowsDebugStateDefinitions()
    }

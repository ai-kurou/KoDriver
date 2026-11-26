package kurou.kodriver.feature.gt7ps5debugstatedetail

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

/** GT7 PS5 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で `named(Simulator.Gt7Ps5.id)` として登録する。 */
public val gt7Ps5DebugStateDetailModule: Module =
    module {
        factory { RequestDebugReadoutUseCase(get()) }
        viewModel { Gt7Ps5DebugStateDetailViewModel(get(), get(named(Simulator.Gt7Ps5.id)), get()) }

        factory(named(Simulator.Gt7Ps5.id)) {
            DebugStateCardOrderUseCases(
                Gt7Ps5DebugStateCardKey.entries,
                ObserveDebugStateCardOrderUseCase(get(named(Simulator.Gt7Ps5.id))),
                ResolveDebugStateCardOrderUseCase(),
                SaveDebugStateCardOrderUseCase(get(named(Simulator.Gt7Ps5.id))),
            )
        }

        gt7Ps5DebugStateDefinitions()
    }

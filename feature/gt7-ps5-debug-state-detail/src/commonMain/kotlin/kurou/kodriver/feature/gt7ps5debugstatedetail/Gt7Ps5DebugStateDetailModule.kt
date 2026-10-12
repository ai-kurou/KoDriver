package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** GT7 PS5 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で `named(Simulator.Gt7Ps5.id)` として登録する。 */
val gt7Ps5DebugStateDetailModule =
    module {
        viewModel { Gt7Ps5DebugStateDetailViewModel(get(), get()) }

        factory {
            DebugStateCardOrderUseCases(
                ObserveDebugStateCardOrderUseCase(get(named(Simulator.Gt7Ps5.id))),
                ResolveDebugStateCardOrderUseCase(),
                SaveDebugStateCardOrderUseCase(get(named(Simulator.Gt7Ps5.id))),
            )
        }

        gt7Ps5DebugStateDefinitions()
    }

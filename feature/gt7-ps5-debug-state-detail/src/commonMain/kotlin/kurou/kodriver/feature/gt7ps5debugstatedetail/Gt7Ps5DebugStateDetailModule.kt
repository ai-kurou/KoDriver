package kurou.kodriver.feature.gt7ps5debugstatedetail

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** GT7 PS5 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で登録する。 */
val gt7Ps5DebugStateDetailModule =
    module {
        viewModel { Gt7Ps5DebugStateDetailViewModel(get()) }
        gt7Ps5DebugStateDefinitions()
    }

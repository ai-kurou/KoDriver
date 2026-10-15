package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** シミュレーター ID に対応する保存済みセットの Repository を使う Koin モジュール。 */
val manualReadoutListModule =
    module {
        viewModel { (simulatorId: String) ->
            ManualReadoutListViewModel(
                simulatorId = simulatorId,
                requestManualReadout = RequestManualReadoutUseCase(get()),
                observeManualReadoutSets = ObserveManualReadoutSetsUseCase(get(named(simulatorId))),
                addManualReadoutSet = AddManualReadoutSetUseCase(get(named(simulatorId))),
                deleteManualReadoutSet = DeleteManualReadoutSetUseCase(get(named(simulatorId))),
            )
        }
    }

package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.manualreadout.usecase.AddManualReadoutSetUseCase
import kurou.kodriver.domain.manualreadout.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutPlayButtonLayoutEnabledUseCase
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.manualreadout.usecase.RequestManualReadoutUseCase
import kurou.kodriver.domain.manualreadout.usecase.SaveManualReadoutPlayButtonLayoutEnabledUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** シミュレーター ID に対応する保存済みセットの Repository を使う Koin モジュール。 */
public val manualReadoutListModule: Module =
    module {
        viewModel { (simulatorId: String) ->
            ManualReadoutListViewModel(
                simulatorId = simulatorId,
                requestManualReadout = RequestManualReadoutUseCase(get()),
                observeManualReadoutSets = ObserveManualReadoutSetsUseCase(get(named(simulatorId))),
                addManualReadoutSet = AddManualReadoutSetUseCase(get(named(simulatorId))),
                deleteManualReadoutSet = DeleteManualReadoutSetUseCase(get(named(simulatorId))),
                playButtonLayoutUseCases =
                    PlayButtonLayoutUseCases(
                        observe = ObserveManualReadoutPlayButtonLayoutEnabledUseCase(get()),
                        save = SaveManualReadoutPlayButtonLayoutEnabledUseCase(get()),
                    ),
            )
        }
    }

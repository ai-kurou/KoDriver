package kurou.kodriver.feature.manualreadoutsetdetail

import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.UpdateManualReadoutSetUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val manualReadoutSetDetailModule =
    module {
        viewModel { (simulatorId: String, setId: String) ->
            ManualReadoutSetDetailViewModel(
                simulatorId = simulatorId,
                setId = setId,
                observeSets = ObserveManualReadoutSetsUseCase(get(named(simulatorId))),
                updateSet = UpdateManualReadoutSetUseCase(get(named(simulatorId))),
                observeDebugHotkeyVirtualKeyCode = ObserveReadoutHotkeyVirtualKeyCodeUseCase(get()),
                deleteSet = DeleteManualReadoutSetUseCase(get(named(simulatorId))),
            )
        }
    }

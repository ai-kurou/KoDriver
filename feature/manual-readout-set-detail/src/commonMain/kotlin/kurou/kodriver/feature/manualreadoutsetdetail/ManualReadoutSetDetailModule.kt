package kurou.kodriver.feature.manualreadoutsetdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.RetryManualReadoutHotkeyRegistrationUseCase
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
                observeHotkeyRegistrationState = ObserveManualReadoutHotkeyRegistrationStateUseCase(get()),
                retryHotkeyRegistration = RetryManualReadoutHotkeyRegistrationUseCase(get()),
                deleteSet = DeleteManualReadoutSetUseCase(get(named(simulatorId))),
                observeOtherSimulatorSets =
                    Simulator.entries
                        .filter { it.id != simulatorId }
                        .associate { it.id to ObserveManualReadoutSetsUseCase(get(named(it.id))) },
            )
        }
    }

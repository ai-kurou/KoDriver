package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.RequestDebugReadoutUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.RetryReadoutHotkeyRegistrationUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveReadoutHotkeyVirtualKeyCodeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** LMU 専用デバッグ状態画面の ViewModel と UseCase を登録する。Repository は利用側で `named(Simulator.LmuWindows.id)` として登録する。 */
val lmuWindowsDebugStateDetailModule =
    module {
        factory { RequestDebugReadoutUseCase(get()) }
        viewModel { LmuWindowsDebugStateDetailViewModel(get(), get(), get(), get()) }

        factory {
            DebugStateCardOrderUseCases(
                ObserveDebugStateCardOrderUseCase(get(named(Simulator.LmuWindows.id))),
                ResolveDebugStateCardOrderUseCase(),
                SaveDebugStateCardOrderUseCase(get(named(Simulator.LmuWindows.id))),
            )
        }

        factory {
            ReadoutHotkeyUseCases(
                ObserveReadoutHotkeyVirtualKeyCodeUseCase(get()),
                SaveReadoutHotkeyVirtualKeyCodeUseCase(get()),
                ObserveReadoutHotkeyRegistrationStateUseCase(get()),
                ObserveReadoutHotkeyUseCase(get()),
                RetryReadoutHotkeyRegistrationUseCase(get()),
            )
        }

        lmuWindowsDebugStateDefinitions()
    }

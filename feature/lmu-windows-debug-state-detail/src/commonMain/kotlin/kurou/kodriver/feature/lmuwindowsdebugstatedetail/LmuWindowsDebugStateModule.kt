package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsClassCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import org.koin.core.module.Module

/**
 * LMU Windows のデバッグ状態用 UseCase 集約と、個別の Observe UseCase を登録する。
 * 各 UseCase が必要とする Repository は composition root で束ねたモジュールから解決する。
 */
internal fun Module.lmuWindowsDebugStateDefinitions() {
    factory {
        LmuWindowsDebugStateUseCases(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    factory { ObserveLmuWindowsCarGapUseCase(get()) }
    factory { ObserveLmuWindowsClassCarGapUseCase(get()) }
    factory { ObserveLmuWindowsRaceFlagsUseCase(get()) }
    factory { ObserveLmuWindowsVirtualEnergyUseCase(get()) }
    factory { ObserveLmuWindowsUseCase(get()) }
    factory { ObserveLmuWindowsVehicleApproachUseCase(get()) }
    factory { ObserveLmuWindowsTyreCarcassTemperatureUseCase(get()) }
    factory { ObserveLmuWindowsBrakeTemperatureUseCase(get()) }
    factory { ObserveLmuWindowsBrakeWearRemainingUseCase(get(), get()) }
    factory { ObserveLmuWindowsVehicleClassUseCase(get()) }
    factory { ObserveLmuWindowsPitStatusUseCase(get()) }
    factory { ObserveLmuWindowsVehicleDamageUseCase(get()) }
    factory { ObserveLmuWindowsTyreDetachedUseCase(get()) }
}

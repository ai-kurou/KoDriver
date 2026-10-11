package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import org.koin.core.module.Module

/**
 * LMU Windows のデバッグ状態用 UseCase 集約と、個別の Observe UseCase を登録する。
 * 各 UseCase が必要とする Repository は composition root で束ねたモジュールから解決する。
 */
internal fun Module.lmuWindowsDebugStateDefinitions() {
    factory {
        LmuWindowsDebugStateUseCases(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
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

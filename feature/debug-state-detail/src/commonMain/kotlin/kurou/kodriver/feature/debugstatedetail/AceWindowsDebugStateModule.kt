package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import org.koin.core.module.Module

/**
 * ACE Windows のデバッグ状態用 UseCase 集約と、個別の Observe UseCase を登録する。
 * 各 UseCase が必要とする Repository は composition root で束ねたモジュールから解決する。
 */
internal fun Module.aceWindowsDebugStateDefinitions() {
    factory { AceWindowsDebugStateUseCases(get(), get(), get(), get(), get(), get(), get()) }
    factory { ObserveAceWindowsFuelUseCase(get()) }
    factory { ObserveAceWindowsFlagUseCase(get()) }
    factory { ObserveAceWindowsStatusUseCase(get()) }
    factory { ObserveAceWindowsTyreCarcassTemperatureUseCase(get()) }
    factory { ObserveAceWindowsVehicleApproachUseCase(get()) }
    factory { ObserveAceWindowsBestLapTimeUseCase(get()) }
    factory { ObserveAceWindowsRemainingFuelLapsUseCase(get()) }
}

package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import org.koin.core.module.Module

/**
 * GT7 PS5 のデバッグ状態用 UseCase 集約と、個別の Observe UseCase を登録する。
 * 各 UseCase が必要とする Repository は composition root で束ねたモジュールから解決する。
 */
internal fun Module.gt7Ps5DebugStateDefinitions() {
    factory { Gt7Ps5DebugStateUseCases(get(), get()) }
    factory { ObserveGt7Ps5UseCase(get()) }
    factory { ObserveGt7Ps5VehicleClassUseCase(get()) }
}

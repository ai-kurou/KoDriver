package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * アナウンス設定一覧（readout-list feature）の Koin モジュール。
 *
 * 提供: ReadoutListViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: SimulatorPreferencesRepository・ReadoutPreferencesRepository・
 *   QueuePreferencesRepository（いずれも :core:data で登録）。
 */
val readoutListModule = module {
    // ViewModel
    viewModelOf(::ReadoutListViewModel)

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveSelectedSimulatorUseCase(get()) }
    factory { SaveSelectedSimulatorUseCase(get()) }
    factory { ObserveReadoutEnabledStatesUseCase(get()) }
    factory { SaveReadoutEnabledStateUseCase(get()) }
    factory { ObserveReadoutOrderUseCase(get()) }
    factory { ResolveReadoutOrderUseCase() }
    factory { SaveReadoutOrderUseCase(get()) }
    factory { ObserveQueueEnabledStatesUseCase(get()) }
    factory { SaveQueueEnabledStateUseCase(get()) }
}

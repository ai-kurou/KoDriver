package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * アナウンス設定一覧（readout-list feature）の Koin モジュール。
 *
 * 提供: ReadoutListViewModel、この feature 内で定義した UseCase 集約 data class
 *   （SimulatorUseCases / ReadoutOrderUseCases / ReadoutEnabledUseCases / QueueUseCases /
 *   StartSoundUseCases）、それらが束ねる各ドメイン UseCase。
 * 消費（get で解決）: SimulatorPreferencesRepository・ReadoutPreferencesRepository・
 *   QueuePreferencesRepository・ReadoutStartSoundEnabledPreferencesRepository（いずれも :core:data で登録）。
 */
val readoutListModule =
    module {
        // ViewModel
        viewModelOf(::ReadoutListViewModel)

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { SimulatorUseCases(get(), get()) }
        factory { ReadoutOrderUseCases(get(), get(), get()) }
        factory { ReadoutEnabledUseCases(get(), get()) }
        factory { QueueUseCases(get(), get()) }
        factory { StartSoundUseCases(get(), get()) }

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
        factory { ObserveReadoutStartSoundEnabledStatesUseCase(get()) }
        factory { SaveReadoutStartSoundEnabledStateUseCase(get()) }
    }

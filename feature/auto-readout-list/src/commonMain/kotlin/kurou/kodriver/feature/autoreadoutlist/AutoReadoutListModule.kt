package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ResolveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveQueueEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * アナウンス設定一覧（auto-readout-list feature）の Koin モジュール。
 *
 * 提供: AutoReadoutListViewModel、この feature 内で定義した UseCase 集約 data class
 *   （SimulatorUseCases / AutoReadoutOrderUseCases / AutoReadoutEnabledUseCases / QueueUseCases /
 *   StartSoundUseCases）、それらが束ねる各ドメイン UseCase。
 * 消費（get で解決）: SimulatorPreferencesRepository・AutoReadoutPreferencesRepository・
 *   QueuePreferencesRepository・ReadoutStartSoundEnabledPreferencesRepository（いずれも :core:data で登録）。
 */
val autoReadoutListModule =
    module {
        // ViewModel
        viewModelOf(::AutoReadoutListViewModel)

        // この feature 固有の UseCase 集約 data class（本モジュールで定義）
        factory { SimulatorUseCases(get()) }
        factory { AutoReadoutOrderUseCases(get(), get(), get()) }
        factory { AutoReadoutEnabledUseCases(get(), get()) }
        factory { QueueUseCases(get(), get()) }
        factory { StartSoundUseCases(get(), get()) }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveAutoReadoutEnabledStatesUseCase(get()) }
        factory { SaveAutoReadoutEnabledStateUseCase(get()) }
        factory { ObserveAutoReadoutOrderUseCase(get()) }
        factory { ResolveAutoReadoutOrderUseCase() }
        factory { SaveAutoReadoutOrderUseCase(get()) }
        factory { ObserveQueueEnabledStatesUseCase(get()) }
        factory { SaveQueueEnabledStateUseCase(get()) }
        factory { ObserveReadoutStartSoundEnabledStatesUseCase(get()) }
        factory { SaveReadoutStartSoundEnabledStateUseCase(get()) }
    }

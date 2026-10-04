package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 燃料残量アナウンス詳細設定（gt7-ps5-readout-remaining-fuel-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutRemainingFuelDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: Gt7Ps5RemainingFuelPreferencesRepository・ReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.Gt7Ps5.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:gt7-ps5-narrator で登録）。
 */
val gt7Ps5ReadoutRemainingFuelDetailModule =
    module {
        viewModel {
            Gt7Ps5ReadoutRemainingFuelDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingFuelReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.Gt7Ps5.id)),
                get(named(Simulator.Gt7Ps5.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveGt7Ps5RemainingFuelReadoutTextUseCase)
        factoryOf(::SaveGt7Ps5RemainingFuelReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase)
        factoryOf(::SaveGt7Ps5RemainingFuelThresholdPercentageUseCase)
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
        factory { RemainingFuelUseCases(get(), get(), get(), get()) }
    }

package kurou.kodriver.feature.gt7ps5autoreadout.remainingfuellapsdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 燃料残り周回数アナウンス詳細設定（gt7-ps5-auto-readout-remaining-fuel-laps-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5AutoReadoutRemainingFuelLapsDetailViewModel と、閾値・有効状態・文言の Observe/Save UseCase と読み上げUseCase集約。
 * 消費（get で解決）: Gt7Ps5RemainingFuelLapsPreferencesRepository・AutoReadoutPreferencesRepository・TextToSpeechRepository・SoundVolumePreferencesRepository（:core:data）、
 *   試聴用の named(Simulator.Gt7Ps5.id) の PlayStartSoundForKeyUseCase・SpeakTextUseCase（:feature:gt7-ps5-narrator で登録）。
 */
val gt7Ps5AutoReadoutRemainingFuelLapsDetailModule =
    module {
        viewModel {
            Gt7Ps5AutoReadoutRemainingFuelLapsDetailViewModel(
                get(),
                get(),
            )
        }

        factory {
            RemainingFuelLapsReadoutUseCases(
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.Gt7Ps5.id)),
                get(named(Simulator.Gt7Ps5.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase)
        factoryOf(::SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase)
        factoryOf(::ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase)
        factoryOf(::SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveGt7Ps5RemainingFuelLapsUseCase)
        factoryOf(::SaveGt7Ps5RemainingFuelLapsUseCase)
        factoryOf(::ObserveAutoReadoutEnabledStatesUseCase)
        factoryOf(::SaveAutoReadoutEnabledStateUseCase)
        factory { RemainingFuelLapsUseCases(get(), get(), get(), get()) }
    }

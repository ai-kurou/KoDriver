package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsYellowFlagReadoutTextUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACEフラッグ詳細設定の Koin モジュール。
 * 提供: ViewModel・有効状態と10種の文言のObserve/Save UseCase・設定UseCase集約・ObserveSoundVolumeUseCase。
 * 消費: AceWindowsFlagPreferencesRepository・AceWindowsFlagReadoutTextPreferencesRepository・
 *   SoundVolumePreferencesRepository（:core:data）、named(Simulator.AceWindows.id) の
 *   PlayStartSoundForKeyUseCase・SpeakTextUseCase・CheckTextToSpeechAvailableUseCase
 *   （:feature:ace-windows-narrator。SpeakTextUseCaseは同名のObserveVoiceUseCaseを利用する）。
 */
val aceWindowsReadoutFlagDetailModule =
    module {
        viewModel {
            AceWindowsReadoutFlagDetailViewModel(
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
            )
        }
        factory { FlagSettingsUseCases(get(), get(), get()) }
        factory {
            FlagReadoutTextUseCases(
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
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
        factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
        factory { SaveAceWindowsFlagEnabledStateUseCase(get()) }
        factory { ObserveAceWindowsCheckeredFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsCheckeredFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsWhiteFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsWhiteFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsGreenFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsGreenFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsRedFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsRedFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsBlueFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsBlueFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsYellowFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsYellowFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsBlackFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsBlackFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase(get()) }
        factory { ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsBlackWhiteFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsOrangeCircleFlagReadoutTextUseCase(get()) }
        factory { SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
    }

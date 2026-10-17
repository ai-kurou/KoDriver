package kurou.kodriver.feature.acewindowsautoreadout.flagdetail

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
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
val aceWindowsAutoReadoutFlagDetailModule =
    module {
        viewModel {
            AceWindowsAutoReadoutFlagDetailViewModel(
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

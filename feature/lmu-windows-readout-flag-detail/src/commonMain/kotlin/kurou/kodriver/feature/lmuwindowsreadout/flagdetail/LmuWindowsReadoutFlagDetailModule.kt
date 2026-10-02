package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * フラグアナウンス詳細設定（lmu-windows-readout-flag-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutFlagDetailViewModel と、それが使うドメイン UseCase。
 * 音声設定監視用の ObserveVoiceUseCase を提供し、VoicePreferencesRepository（:core:data）を消費する。
 * 消費（get で解決）: LmuWindowsFlagPreferencesRepository・
 *   LmuWindowsFlagReadoutTextPreferencesRepository・TextToSpeechRepository（:core:data /
 *   :core:text-to-speech-data）、試聴用の named(Simulator.LmuWindows.id) の
 *   PlayStartSoundForKeyUseCase（いずれも :feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutFlagDetailModule =
    module {
        // ViewModel（get(named(Simulator.LmuWindows.id)) は narrator モジュールの
        // PlayStartSoundForKeyUseCase を解決）
        viewModel {
            LmuWindowsReadoutFlagDetailViewModel(
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }

        factory { ObserveVoiceUseCase(get()) }
        factory { FlagSettingsUseCases(get(), get(), get()) }
        factory { FlagReadoutTextUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
        factory { SaveLmuWindowsFlagEnabledStateUseCase(get()) }
        factory { ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(get()) }
        factory { ObserveLmuWindowsBlueFlagReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsBlueFlagReadoutTextUseCase(get()) }
        factory { ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(get()) }
        factory { ObserveLmuWindowsRedFlagReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsRedFlagReadoutTextUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SpeakTextUseCase(get(), get()) }
        factory { CheckTextToSpeechAvailableUseCase(get()) }
    }

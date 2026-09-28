package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * フラグアナウンス詳細設定（lmu-windows-readout-flag-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutFlagDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsFlagPreferencesRepository・LmuWindowsRedFlagPreferencesRepository・
 *   LmuWindowsFlagReadoutTextPreferencesRepository・TextToSpeechRepository（:core:data /
 *   :core:text-to-speech-data）、試聴用の named(Simulator.LmuWindows.id) の PlaySpeechEventUseCase・
 *   PlayStartSoundForKeyUseCase（いずれも :feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutFlagDetailModule =
    module {
        // ViewModel（get(named(Simulator.LmuWindows.id)) は narrator モジュールの
        // PlaySpeechEventUseCase・PlayStartSoundForKeyUseCase を解決）
        viewModel {
            LmuWindowsReadoutFlagDetailViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(),
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveLmuWindowsFlagEnabledStatesUseCase(get()) }
        factory { ObserveLmuWindowsRedFlagVoiceTypeUseCase(get()) }
        factory { SaveLmuWindowsFlagEnabledStateUseCase(get()) }
        factory { SaveLmuWindowsRedFlagVoiceTypeUseCase(get()) }
        factory { ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(get()) }
        factory { SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(get()) }
        factory { SpeakTextUseCase(get()) }
        factory { CheckTextToSpeechAvailableUseCase(get()) }
    }

package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 燃料残り周回数アナウンス詳細設定（ace-windows-readout-remaining-fuel-laps-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutRemainingFuelLapsDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsRemainingFuelLapsPreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) の TextToSpeechEngine（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutRemainingFuelLapsDetailModule =
    module {
        // ViewModel（get(named(Simulator.AceWindows.id)) は narrator モジュールの TextToSpeechEngine を解決）
        viewModel {
            AceWindowsReadoutRemainingFuelLapsDetailViewModel(get(), get(), get(named(Simulator.AceWindows.id)))
        }

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveAceWindowsRemainingFuelLapsThresholdUseCase(get()) }
        factory { SaveAceWindowsRemainingFuelLapsThresholdUseCase(get()) }
    }

package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * フラグアナウンス詳細設定（ace-windows-readout-flag-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutFlagDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsFlagPreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.AceWindows.id) の PlaySpeechEventUseCase（:feature:ace-windows-narrator で登録）。
 */
val aceWindowsReadoutFlagDetailModule =
    module {
        // ViewModel（get(named "ace_windows") は narrator モジュールの PlaySpeechEventUseCase を解決）
        viewModel { AceWindowsReadoutFlagDetailViewModel(get(), get(), get(named(Simulator.AceWindows.id))) }

        factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
        factory { SaveAceWindowsFlagEnabledStateUseCase(get()) }
    }

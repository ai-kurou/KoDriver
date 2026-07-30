package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * フラグアナウンス詳細設定（ace-windows-readout-flag-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutFlagDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsFlagPreferencesRepository（:core:data）。
 */
val aceWindowsReadoutFlagDetailModule = module {
    viewModel { AceWindowsReadoutFlagDetailViewModel(get(), get()) }

    factory { ObserveAceWindowsFlagEnabledStatesUseCase(get()) }
    factory { SaveAceWindowsFlagEnabledStateUseCase(get()) }
}

package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 残り燃料アナウンス詳細設定（ace-windows-readout-remaining-fuel-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsReadoutRemainingFuelDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsRemainingFuelPreferencesRepository（:core:data）。
 */
val aceWindowsReadoutRemainingFuelDetailModule = module {
    viewModel {
        AceWindowsReadoutRemainingFuelDetailViewModel(get(), get())
    }

    factoryOf(::ObserveAceWindowsRemainingFuelThresholdPercentageUseCase)
    factoryOf(::SaveAceWindowsRemainingFuelThresholdPercentageUseCase)
}

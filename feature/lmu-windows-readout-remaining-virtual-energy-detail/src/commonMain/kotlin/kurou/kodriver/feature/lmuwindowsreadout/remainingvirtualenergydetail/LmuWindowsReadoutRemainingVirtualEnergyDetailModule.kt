package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * バーチャルエナジー残量アナウンス詳細設定（lmu-windows-readout-remaining-virtual-energy-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsRemainingVirtualEnergyPreferencesRepository（:core:data）、試聴用の
 *   named("lmu_windows") の PlaySpeechEventUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutRemainingVirtualEnergyDetailModule =
    module {
    viewModel {
        LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(get(), get(), get(named("lmu_windows")))
    }

    factoryOf(::ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase)
    factoryOf(::SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase)
}

package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyLapsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * LMU バーチャルエナジー残り周回数アナウンス詳細設定（lmu-windows-readout-remaining-virtual-energy-laps-detail
 * feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository（:core:data）。
 */
val lmuWindowsReadoutRemainingVirtualEnergyLapsDetailModule = module {
    viewModel { LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModel(get(), get()) }

    factory { ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase(get()) }
    factory { SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(get()) }
}

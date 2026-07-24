package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * バーチャルエナジー残量アナウンス詳細設定（lmu-windows-readout-remaining-virtual-energy-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel。
 * 消費（get で解決）: 試聴用の named("lmu_windows") の PlaySpeechEventUseCase（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutRemainingVirtualEnergyDetailModule = module {
    viewModel {
        LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(get(named("lmu_windows")))
    }
}

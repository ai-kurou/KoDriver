package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * バーチャルエナジー残量アナウンス詳細設定（lmu-windows-readout-remaining-virtual-energy-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel。
 * 現時点では設定項目を持たないため、依存する UseCase はない。
 */
val lmuWindowsReadoutRemainingVirtualEnergyDetailModule = module {
    viewModelOf(::LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel)
}

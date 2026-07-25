package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ピットタイミングアナウンス詳細設定（lmu-windows-readout-pit-timing-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutPitTimingDetailViewModel（見た目のみの実装のため、外部依存なし）。
 */
val lmuWindowsReadoutPitTimingDetailModule = module {
    viewModel {
        LmuWindowsReadoutPitTimingDetailViewModel()
    }
}

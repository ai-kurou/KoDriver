package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * GT7 タイヤ温度アナウンス詳細設定（gt7-ps5-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutTyreTemperatureDetailViewModel。設定値の永続化は未実装のため、UseCase・
 *   Repository の解決は行わない（別PRで追加予定）。
 */
val gt7Ps5ReadoutTyreTemperatureDetailModule =
    module {
        viewModel { Gt7Ps5ReadoutTyreTemperatureDetailViewModel() }
    }

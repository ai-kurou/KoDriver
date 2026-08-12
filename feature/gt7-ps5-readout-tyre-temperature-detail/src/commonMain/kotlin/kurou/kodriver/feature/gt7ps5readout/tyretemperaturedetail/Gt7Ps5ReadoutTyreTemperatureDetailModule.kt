package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * GT7 タイヤ温度アナウンス詳細設定（gt7-ps5-readout-tyre-temperature-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutTyreTemperatureDetailViewModel。高温閾値の永続化用 UseCase を解決する。
 */
val gt7Ps5ReadoutTyreTemperatureDetailModule =
    module {
        viewModel { Gt7Ps5ReadoutTyreTemperatureDetailViewModel(get(), get()) }

        factoryOf(::ObserveGt7Ps5TyreTemperatureHighThresholdUseCase)
        factoryOf(::SaveGt7Ps5TyreTemperatureHighThresholdUseCase)
    }

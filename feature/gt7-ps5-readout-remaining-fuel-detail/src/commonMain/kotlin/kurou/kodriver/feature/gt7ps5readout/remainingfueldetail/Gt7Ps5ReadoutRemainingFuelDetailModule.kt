package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 燃料残量アナウンス詳細設定（gt7-ps5-readout-remaining-fuel-detail feature）の Koin モジュール。
 */
val gt7Ps5ReadoutRemainingFuelDetailModule =
    module {
    viewModel {
        Gt7Ps5ReadoutRemainingFuelDetailViewModel(get(), get(), get(named("gt7_ps5")))
    }

    factoryOf(::ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase)
    factoryOf(::SaveGt7Ps5RemainingFuelThresholdPercentageUseCase)
}

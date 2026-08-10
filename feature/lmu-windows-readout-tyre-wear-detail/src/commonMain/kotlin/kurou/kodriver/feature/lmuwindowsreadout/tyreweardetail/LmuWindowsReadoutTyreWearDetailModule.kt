package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * タイヤ摩耗アナウンス詳細設定（lmu-windows-readout-tyre-wear-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutTyreWearDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsTyreWearPreferencesRepository（:core:data）、試聴用の
 *   named(Simulator.LmuWindows.id) の TextToSpeechEngine（:feature:lmu-windows-narrator で登録）。
 */
val lmuWindowsReadoutTyreWearDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutTyreWearDetailViewModel(get(), get(), get(named(Simulator.LmuWindows.id)))
        }

        factoryOf(::ObserveLmuWindowsTyreWearThresholdPercentageUseCase)
        factoryOf(::SaveLmuWindowsTyreWearThresholdPercentageUseCase)
    }

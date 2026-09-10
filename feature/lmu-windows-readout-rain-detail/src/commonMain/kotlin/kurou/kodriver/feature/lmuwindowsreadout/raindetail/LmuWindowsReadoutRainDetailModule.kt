package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsRainEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRainEnabledStateUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 降雨アナウンス詳細設定（lmu-windows-readout-rain-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutRainDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: LmuWindowsRainPreferencesRepository（:core:data）。
 *
 * Narrator側の実際の読み上げ判定ロジックへの配線は未実装（#1500）。
 */
val lmuWindowsReadoutRainDetailModule =
    module {
        viewModelOf(::LmuWindowsReadoutRainDetailViewModel)

        factory { ObserveLmuWindowsRainEnabledStatesUseCase(get()) }
        factory { SaveLmuWindowsRainEnabledStateUseCase(get()) }
    }

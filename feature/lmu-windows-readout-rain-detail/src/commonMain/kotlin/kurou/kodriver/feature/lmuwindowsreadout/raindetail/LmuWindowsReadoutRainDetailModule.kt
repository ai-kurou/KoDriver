package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 降雨アナウンス詳細設定（lmu-windows-readout-rain-detail feature）の Koin モジュール。
 *
 * 永続化は未実装（GUIのみ先行実装、#1500）のため、依存する Repository・UseCase はない。
 */
val lmuWindowsReadoutRainDetailModule =
    module {
        viewModelOf(::LmuWindowsReadoutRainDetailViewModel)
    }

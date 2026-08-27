package kurou.kodriver.feature.main

import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.HAPTIC_FEEDBACK_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT
import kurou.kodriver.domain.model.Simulator

/**
 * AppScreen 画面の表示状態。
 */
data class AppScreenUiState(
    val hasAppUpdate: Boolean = false,
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
    val hapticFeedbackEnabled: Boolean = HAPTIC_FEEDBACK_ENABLED_DEFAULT,
    val selectedSimulator: Simulator = Simulator.LmuWindows,
) {
    /**
     * app:shared など `:core:domain` に依存しないモジュールへ渡すための、選択中シミュレータの ID。
     * `:core:domain` の `Simulator` 型は `implementation` 依存のため、モジュール境界を越えて型を渡せない。
     */
    val selectedSimulatorId: String
        get() = selectedSimulator.id
}

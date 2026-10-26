package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase

/** 端末設定の観測・保存と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListDeviceSettingsFlow(
    private val observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    private val saveKeepScreenOn: SaveKeepScreenOnEnabledUseCase,
    private val observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    private val saveDynamicColorEnabled: SaveDynamicColorEnabledUseCase,
    private val observeHapticFeedbackEnabled: ObserveHapticFeedbackEnabledUseCase,
    private val saveHapticFeedbackEnabled: SaveHapticFeedbackEnabledUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
) {
    val uiState: Flow<OtherListDeviceSettingsUiState> =
        combine(
            observeKeepScreenOn(),
            observeDynamicColorEnabled(),
            observeHapticFeedbackEnabled(),
            observeThemeMode(),
        ) { keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled, themeMode ->
            OtherListDeviceSettingsUiState(keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled, themeMode)
        }

    suspend fun onKeepScreenOnChange(enabled: Boolean) = saveKeepScreenOn(enabled)

    suspend fun onDynamicColorEnabledChange(enabled: Boolean) = saveDynamicColorEnabled(enabled)

    suspend fun onHapticFeedbackEnabledChange(enabled: Boolean) = saveHapticFeedbackEnabled(enabled)
}

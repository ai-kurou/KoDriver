package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase

/** 端末設定の観測・保存と表示状態を管理する。実行のライフサイクルは呼び出し元に従う。 */
@Suppress("LongParameterList")
public class OtherListDeviceSettingsFlow(
    private val observeKeepScreenOn: ObserveKeepScreenOnEnabledUseCase,
    private val saveKeepScreenOn: SaveKeepScreenOnEnabledUseCase,
    private val observeDynamicColorEnabled: ObserveDynamicColorEnabledUseCase,
    private val saveDynamicColorEnabled: SaveDynamicColorEnabledUseCase,
    private val observeHapticFeedbackEnabled: ObserveHapticFeedbackEnabledUseCase,
    private val saveHapticFeedbackEnabled: SaveHapticFeedbackEnabledUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val observeMicrophoneCutQuietSoundLevel: ObserveMicrophoneCutQuietSoundLevelUseCase,
) {
    public val uiState: Flow<OtherListDeviceSettingsUiState> =
        combine(
            observeKeepScreenOn(),
            observeDynamicColorEnabled(),
            observeHapticFeedbackEnabled(),
            observeThemeMode(),
            observeMicrophoneCutQuietSoundLevel(),
        ) { keepScreenOn, dynamicColorEnabled, hapticFeedbackEnabled, themeMode, microphoneCutQuietSoundLevel ->
            OtherListDeviceSettingsUiState(
                keepScreenOn,
                dynamicColorEnabled,
                hapticFeedbackEnabled,
                themeMode,
                microphoneCutQuietSoundLevel,
            )
        }

    public suspend fun onKeepScreenOnChange(enabled: Boolean): Unit = saveKeepScreenOn(enabled)

    public suspend fun onDynamicColorEnabledChange(enabled: Boolean): Unit = saveDynamicColorEnabled(enabled)

    public suspend fun onHapticFeedbackEnabledChange(enabled: Boolean): Unit = saveHapticFeedbackEnabled(enabled)
}

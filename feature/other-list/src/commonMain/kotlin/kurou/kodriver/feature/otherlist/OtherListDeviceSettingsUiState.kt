package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.HAPTIC_FEEDBACK_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.ThemeMode

/** 端末設定フローの表示状態。 */
data class OtherListDeviceSettingsUiState(
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
    val hapticFeedbackEnabled: Boolean = HAPTIC_FEEDBACK_ENABLED_DEFAULT,
    val themeMode: ThemeMode = THEME_MODE_DEFAULT,
)

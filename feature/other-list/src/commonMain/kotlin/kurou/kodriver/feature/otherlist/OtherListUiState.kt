package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.HAPTIC_FEEDBACK_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT

/**
 * OtherList 画面の表示状態。
 */
data class OtherListUiState(
    val items: List<OtherListItemType> = buildOtherListItems(),
    val selectedItem: OtherListItemType? = null,
    val selectedFeedbackTelemetryLogId: Long? = null,
    // selectFeedbackItem のたびに増分する。同じログIDを再選択した場合でも
    // uiState の値を必ず変化させ、detailContent 側の LaunchedEffect(telemetryLogId) を再実行させるために使う。
    val feedbackAttachRequestId: Long = 0,
    val hasAppUpdate: Boolean = false,
    val accessLocalNetworkPermissionGranted: Boolean = true,
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
    val hapticFeedbackEnabled: Boolean = HAPTIC_FEEDBACK_ENABLED_DEFAULT,
    val startupEnabled: Boolean = false,
    val appVersionLabel: String = "",
    val appVersion: String = "",
)

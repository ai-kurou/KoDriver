package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT
import kurou.kodriver.domain.model.HAPTIC_FEEDBACK_ENABLED_DEFAULT
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_VISIBLE_DEFAULT
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.SOUND_VOLUME_DEFAULT
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT

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
    val overlayVisible: Boolean = OVERLAY_VISIBLE_DEFAULT,
    val overlayTextSize: OverlayTextSize = OVERLAY_TEXT_SIZE_DEFAULT,
    val overlayBackgroundOpacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
    val keepScreenOn: Boolean = KEEP_SCREEN_ON_ENABLED_DEFAULT,
    val dynamicColorEnabled: Boolean = DYNAMIC_COLOR_ENABLED_DEFAULT,
    val hapticFeedbackEnabled: Boolean = HAPTIC_FEEDBACK_ENABLED_DEFAULT,
    val soundVolume: Int = SOUND_VOLUME_DEFAULT,
    val deviceVolume: Int = DEVICE_VOLUME_MIN,
    val readoutStartSoundType: ReadoutStartSoundType = READOUT_START_SOUND_TYPE_DEFAULT,
    val voiceId: String = VOICE_ID_UNSPECIFIED,
    val voiceSpeed: Float = VOICE_SPEED_DEFAULT,
    val voicePitch: Float = VOICE_PITCH_DEFAULT,
    val serverIp: String? = null,
    val consoleAddress: String? = null,
    val consolePort: Int = GT7_PS5_UDP_PORT_DEFAULT,
    val themeMode: ThemeMode = THEME_MODE_DEFAULT,
    val ttsUnavailableGuidance: TtsUnavailableGuidance? = null,
    val startupEnabled: Boolean = false,
    val appVersionLabel: String = "",
    val appVersion: String = "",
)

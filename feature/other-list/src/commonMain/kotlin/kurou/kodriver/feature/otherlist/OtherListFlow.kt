package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 一覧の各入力を表示状態へ合成する冷たい Flow。共有と実行のライフサイクルは呼び出し元が管理する。 */
@Suppress("LongParameterList")
class OtherListFlow(
    baseState: Flow<OtherListUiState>,
    overlaySettings: Flow<OtherListOverlaySettingsUiState>,
    deviceSettings: Flow<OtherListDeviceSettingsUiState>,
    speechSettings: Flow<OtherListSpeechSettingsUiState>,
    connection: Flow<OtherListConnectionUiState>,
    deviceVolume: Flow<Int>,
    ttsAvailability: Flow<OtherListTtsAvailabilityUiState>,
    applyGuidance: (List<OtherListItemType>) -> List<OtherListItemType>,
    reset: Flow<OtherListResetUiState>,
    autoReadoutSettingsReset: Flow<OtherListResetUiState>,
    startupRegistration: Flow<OtherListStartupRegistrationUiState>,
    appUpdate: Flow<OtherListAppUpdateUiState>,
) {
    val uiState: Flow<OtherListUiState> =
        combine(
            baseState,
            overlaySettings,
            deviceSettings,
            speechSettings,
            connection,
        ) { state, overlaySettings, deviceSettings, speechSettings, connection ->
            state.copy(
                overlayVisible = overlaySettings.overlayVisible,
                overlayTextSize = overlaySettings.overlayTextSize,
                overlayTextColor = overlaySettings.overlayTextColor,
                overlayBackgroundOpacity = overlaySettings.overlayBackgroundOpacity,
                keepScreenOn = deviceSettings.keepScreenOn,
                dynamicColorEnabled = deviceSettings.dynamicColorEnabled,
                hapticFeedbackEnabled = deviceSettings.hapticFeedbackEnabled,
                themeMode = deviceSettings.themeMode,
                voiceId = speechSettings.voiceId,
                voiceSpeed = speechSettings.voiceSpeed,
                voicePitch = speechSettings.voicePitch,
                androidAudioUsage = speechSettings.androidAudioUsage,
                readoutStartSoundType = speechSettings.readoutStartSoundType,
                soundVolume = speechSettings.soundVolume,
                serverIp = connection.serverIp,
                consoleAddress = connection.consoleAddress,
                consolePort = connection.consolePort,
            )
        }.combine(deviceVolume) { state, deviceVolume ->
            state.copy(deviceVolume = deviceVolume)
        }.combine(ttsAvailability) { state, ttsAvailability ->
            state.copy(
                items = applyGuidance(state.items),
                ttsUnavailableGuidance = ttsAvailability.ttsUnavailableGuidance,
            )
        }.combine(reset) { state, reset ->
            state.copy(reset = reset)
        }.combine(autoReadoutSettingsReset) { state, reset ->
            state.copy(autoReadoutSettingsReset = reset)
        }.combine(startupRegistration) { state, startupRegistration ->
            state.copy(startupEnabled = startupRegistration.startupEnabled)
        }.combine(appUpdate) { state, appUpdate ->
            state.copy(hasAppUpdate = appUpdate.hasAppUpdate)
        }
}

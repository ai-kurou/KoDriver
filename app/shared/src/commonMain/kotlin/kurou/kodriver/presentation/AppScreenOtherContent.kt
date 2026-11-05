package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kurou.kodriver.feature.acewindowsdebugstatedetail.AceWindowsDebugStateDetailPane
import kurou.kodriver.feature.gt7ps5debugstatedetail.Gt7Ps5DebugStateDetailPane
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.LmuWindowsDebugStateDetailPane
import kurou.kodriver.feature.otherandroidaudiousagedetail.OtherAndroidAudioUsageDetailPane
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPane
import kurou.kodriver.feature.otherfeedbackdetail.OtherFeedbackDetailPane
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.OtherOverlayBackgroundOpacityDetailPane
import kurou.kodriver.feature.otheroverlaytextcolordetail.OtherOverlayTextColorDetailDialog
import kurou.kodriver.feature.otheroverlaytextsizedetail.OtherOverlayTextSizeDetailDialog
import kurou.kodriver.feature.otherreadoutstartsounddetail.OtherReadoutStartSoundDetailDialog
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailPane
import kurou.kodriver.feature.otherthemedetail.OtherThemeDetailDialog

/**
 * `:core:domain` の `Simulator.LmuWindows.id` と一致させる、初期選択シミュレーターの ID。
 * app:shared は `:core:domain` に依存しないため、Preview / デフォルト値用にリテラルで保持する。
 */
internal const val LMU_WINDOWS_SIMULATOR_ID = "lmu_windows"
internal const val GT7_PS5_SIMULATOR_ID = "gt7_ps5"
internal const val ACE_WINDOWS_SIMULATOR_ID = "ace_windows"

internal enum class DebugStateDetailType { LMU_WINDOWS, GT7_PS5, ACE_WINDOWS, UNKNOWN }

internal fun debugStateDetailType(selectedSimulatorId: String): DebugStateDetailType =
    when (selectedSimulatorId) {
        LMU_WINDOWS_SIMULATOR_ID -> DebugStateDetailType.LMU_WINDOWS
        GT7_PS5_SIMULATOR_ID -> DebugStateDetailType.GT7_PS5
        ACE_WINDOWS_SIMULATOR_ID -> DebugStateDetailType.ACE_WINDOWS
        else -> DebugStateDetailType.UNKNOWN
    }

@Composable
internal fun DebugStateDetailPane(
    selectedSimulatorId: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    lmuWindowsPane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        LmuWindowsDebugStateDetailPane(canBack, back)
    },
    gt7Ps5Pane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        Gt7Ps5DebugStateDetailPane(canBack, back)
    },
    aceWindowsPane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        AceWindowsDebugStateDetailPane(canBack, back)
    },
) {
    when (debugStateDetailType(selectedSimulatorId)) {
        DebugStateDetailType.LMU_WINDOWS -> lmuWindowsPane(canNavigateBack, onBack)
        DebugStateDetailType.GT7_PS5 -> gt7Ps5Pane(canNavigateBack, onBack)
        DebugStateDetailType.ACE_WINDOWS -> aceWindowsPane(canNavigateBack, onBack)
        DebugStateDetailType.UNKNOWN -> Unit
    }
}

@Composable
internal fun DefaultOtherContent(
    selectedSimulatorId: String,
    backHandler: AppBackHandler,
    scrollToTopRequest: Int,
) {
    var showReadoutStartSoundDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showOverlayTextColorDialog by rememberSaveable { mutableStateOf(false) }
    var showOverlayTextSizeDialog by rememberSaveable { mutableStateOf(false) }
    if (showReadoutStartSoundDialog) {
        OtherReadoutStartSoundDetailDialog(
            simulatorId = selectedSimulatorId,
            onDismiss = { showReadoutStartSoundDialog = false },
        )
    }
    if (showThemeDialog) {
        OtherThemeDetailDialog(onDismiss = { showThemeDialog = false })
    }
    if (showOverlayTextColorDialog) {
        OtherOverlayTextColorDetailDialog(onDismiss = { showOverlayTextColorDialog = false })
    }
    if (showOverlayTextSizeDialog) {
        OtherOverlayTextSizeDetailDialog(onDismiss = { showOverlayTextSizeDialog = false })
    }
    OtherContent(
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        onOpenReadoutStartSoundDialog = { showReadoutStartSoundDialog = true },
        onOpenThemeDialog = { showThemeDialog = true },
        onOpenOverlayTextColorDialog = { showOverlayTextColorDialog = true },
        onOpenOverlayTextSizeDialog = { showOverlayTextSizeDialog = true },
        detailContent = { itemType, canNavigateBack, onBack, feedbackTelemetryLogId, feedbackAttachRequestId ->
            DefaultOtherDetailContent(
                itemType = itemType,
                selectedSimulatorId = selectedSimulatorId,
                canNavigateBack = canNavigateBack,
                onBack = onBack,
                feedbackTelemetryLogId = feedbackTelemetryLogId,
                feedbackAttachRequestId = feedbackAttachRequestId,
            )
        },
    )
}

@Composable
internal fun DefaultOtherDetailContent(
    itemType: OtherListItemType,
    selectedSimulatorId: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    feedbackTelemetryLogId: Long?,
    feedbackAttachRequestId: Long,
) {
    when (itemType) {
        OtherListItemType.ServerIp -> {
            OtherServerIpDetailPane(canNavigateBack, onBack)
        }

        OtherListItemType.ConsoleIp -> {
            OtherConsoleIpDetailPane(canNavigateBack, onBack)
        }

        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        -> {
            OtherReadoutSettingsDetailPane(itemType, canNavigateBack, onBack)
        }

        OtherListItemType.OverlayBackgroundOpacity -> {
            OtherOverlayBackgroundOpacityDetailPane(canNavigateBack, onBack)
        }

        OtherListItemType.License -> {
            OtherLicenseDetailPane(canNavigateBack, onBack)
        }

        OtherListItemType.Feedback -> {
            OtherFeedbackDetailPane(
                canNavigateBack,
                onBack,
                telemetryLogId = feedbackTelemetryLogId,
                telemetryLogAttachRequestId = feedbackAttachRequestId,
            )
        }

        OtherListItemType.DebugState -> {
            DebugStateDetailPane(selectedSimulatorId, canNavigateBack, onBack)
        }

        OtherListItemType.AndroidAudioUsage -> {
            OtherAndroidAudioUsageDetailPane(canNavigateBack, onBack)
        }

        OtherListItemType.DeleteAllLogs,
        OtherListItemType.ResetAutoReadoutSettings,
        OtherListItemType.ResetManualReadoutSettings,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayVisible,
        OtherListItemType.KeepScreenOn,
        OtherListItemType.ReadoutStartSound,
        OtherListItemType.Theme,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
        OtherListItemType.Startup,
        OtherListItemType.GitHubRepository,
        OtherListItemType.ReleasePage,
        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.TtsEngineMissing,
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.WindowsSpeechUnavailable,
        -> {
        }
    }
}

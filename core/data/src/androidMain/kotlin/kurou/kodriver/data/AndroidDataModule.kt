package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import kurou.kodriver.data.debugreadout.InMemoryDebugReadoutRequestRepository
import kurou.kodriver.data.device.AndroidAccessLocalNetworkPermissionRepository
import kurou.kodriver.data.device.AndroidHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.hotkey.UnsupportedManualReadoutHotkeyRepository
import kurou.kodriver.data.hotkey.UnsupportedWakeWordHotkeyRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutItemRequestRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutItemSpokenRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutRequestRepository
import kurou.kodriver.data.manualreadout.InMemorySpeechPlaybackStateRepository
import kurou.kodriver.data.manualreadout.InMemorySpeechRecognitionEnabledRepository
import kurou.kodriver.data.manualreadout.InMemorySpeechRecognitionTranscriptRepository
import kurou.kodriver.data.preferences.AndroidAutoReadoutPreferencesRepository
import kurou.kodriver.data.preferences.AndroidDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.AndroidHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.AndroidKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.AndroidServerIpPreferencesRepository
import kurou.kodriver.data.preferences.AndroidSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.androidPreferencesCorruptionHandler
import kurou.kodriver.data.preferences.createAndroidAudioUsagePreferencesRepository
import kurou.kodriver.data.preferences.createConsoleAddressPreferencesRepository
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createFeedbackCooldownPreferencesRepository
import kurou.kodriver.data.preferences.createLowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createMainWindowBoundsPreferencesRepository
import kurou.kodriver.data.preferences.createManualReadoutPlayButtonLayoutPreferencesRepository
import kurou.kodriver.data.preferences.createManualReadoutSetPreferencesRepository
import kurou.kodriver.data.preferences.createMicrophoneCutQuietSoundPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayTextColorPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayTextSizePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayVisiblePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayWindowBoundsPreferencesRepository
import kurou.kodriver.data.preferences.createQueuePreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundPreferencesRepository
import kurou.kodriver.data.preferences.createSoundVolumePreferencesRepository
import kurou.kodriver.data.preferences.createThemePreferencesRepository
import kurou.kodriver.data.preferences.createVoicePitchPreferencesRepository
import kurou.kodriver.data.preferences.createVoicePreferencesRepository
import kurou.kodriver.data.preferences.createVoiceSpeedPreferencesRepository
import kurou.kodriver.data.preferences.createWakeWordHotkeyPreferencesRepository
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.release.HttpServerVersionRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.data.websocket.createWebSocketHttpClient
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.autoreadout.repository.AutoReadoutSettingsResetter
import kurou.kodriver.domain.debug.repository.DebugReadoutRequestRepository
import kurou.kodriver.domain.debug.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.feedback.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.feedback.repository.FeedbackSenderRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemRequestRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutItemSpokenRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSettingsResetter
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.overlay.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kurou.kodriver.domain.serverconnection.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.serverconnection.repository.ServerVersionRepository
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionTranscriptRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore(
    name = "simulator_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)
private val Context.readoutDataStore by preferencesDataStore(
    name = "auto_readout_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)
private val Context.serverIpDataStore by preferencesDataStore(
    name = "server_ip_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)
private val Context.keepScreenOnDataStore by preferencesDataStore(
    name = "keep_screen_on_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)
private val Context.dynamicColorDataStore by preferencesDataStore(
    name = "dynamic_color_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)
private val Context.hapticFeedbackEnabledDataStore by preferencesDataStore(
    name = "haptic_feedback_enabled_preferences",
    corruptionHandler = androidPreferencesCorruptionHandler,
)

/**
 * Android 版の Repository バインドを行う Koin モジュール（:core:data / androidMain）。
 *
 * app エントリーポイント（androidApp）で composition root として束ねられ、各 feature モジュールの
 * UseCase が get() で解決する Repository 実装を提供する。デスクトップ版（DesktopDataModule）との違いは、
 * LMU の走行データは別登録の `androidLmuWindowsDataModule` が **KoDriver サーバーへの WebSocket** から取得する。
 * 大半は DataStore バインドで、ServerVersion/AppUpdate はネットワーク、TelemetryLog は Room DB。
 */
public fun androidDataModule(context: Context): Module =
    module {
        single {
            CoreAutoReadoutSettingsResetter(
                autoReadoutPreferencesRepository = get(),
                queuePreferencesRepository = get(),
                readoutStartSoundEnabledPreferencesRepository = get(),
            )
        } bind AutoReadoutSettingsResetter::class
        single {
            CoreManualReadoutSettingsResetter(
                lmuWindowsManualReadoutSetPreferencesRepository = get(named(Simulator.LmuWindows.id)),
                aceWindowsManualReadoutSetPreferencesRepository = get(named(Simulator.AceWindows.id)),
                gt7Ps5ManualReadoutSetPreferencesRepository = get(named(Simulator.Gt7Ps5.id)),
                manualReadoutPlayButtonLayoutEnabledPreferencesRepository = get(),
            )
        } bind ManualReadoutSettingsResetter::class
        single<DebugReadoutRequestRepository> { InMemoryDebugReadoutRequestRepository() }
        single<ManualReadoutHotkeyRepository> { UnsupportedManualReadoutHotkeyRepository() }
        single<WakeWordHotkeyRepository> { UnsupportedWakeWordHotkeyRepository() }
        single<ManualReadoutRequestRepository> { InMemoryManualReadoutRequestRepository() }
        single<ManualReadoutItemRequestRepository> { InMemoryManualReadoutItemRequestRepository() }
        single<ManualReadoutItemSpokenRepository> { InMemoryManualReadoutItemSpokenRepository() }
        single<SpeechPlaybackStateRepository> { InMemorySpeechPlaybackStateRepository() }
        single<SpeechRecognitionEnabledRepository> { InMemorySpeechRecognitionEnabledRepository() }
        single<SpeechRecognitionTranscriptRepository> { InMemorySpeechRecognitionTranscriptRepository() }
        single<Context> { context }

        // 設定永続化（DataStore。ファイルは context.filesDir 配下）
        single<SimulatorPreferencesRepository> {
            AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
        }
        single<AutoReadoutPreferencesRepository> {
            AndroidAutoReadoutPreferencesRepository(context.readoutDataStore)
        }
        single<QueuePreferencesRepository> {
            createQueuePreferencesRepository(context.filesDir.absolutePath)
        }
        single<AndroidAudioUsagePreferencesRepository> {
            createAndroidAudioUsagePreferencesRepository(context.filesDir.absolutePath)
        }
        // :core:lmu-windows-data / :core:ace-windows-data の Android WebSocket リポジトリは、このバインドを get() で解決して共有する（WebSockets プラグインと ping 間隔の設定を含むため、削除すると WebSocket が動かなくなる）。
        single<HttpClient> { createWebSocketHttpClient() }
        includes(androidDataModuleCommonSimulatorPreferences(context))
        includes(androidDataModuleCommonPreferences(context))
        includes(androidDataModuleAppSettings(context))
        includes(androidDataModuleMisc(context))
    }

/**
 * androidDataModule から分離した各種 DataStore バインドとネットワーク系バインド（LongMethod 対策）。
 */
private fun androidDataModuleMisc(context: Context) =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.LmuWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath, Simulator.LmuWindows)
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) {
            createManualReadoutSetPreferencesRepository(context.filesDir.absolutePath, Simulator.LmuWindows)
        }
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.AceWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath, Simulator.AceWindows)
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.AceWindows.id)) {
            createManualReadoutSetPreferencesRepository(context.filesDir.absolutePath, Simulator.AceWindows)
        }
        single<SoundVolumePreferencesRepository> {
            createSoundVolumePreferencesRepository(context.filesDir.absolutePath)
        }
        single<VoiceSpeedPreferencesRepository> {
            createVoiceSpeedPreferencesRepository(context.filesDir.absolutePath)
        }
        single<VoicePitchPreferencesRepository> {
            createVoicePitchPreferencesRepository(context.filesDir.absolutePath)
        }
        single<VoicePreferencesRepository> {
            createVoicePreferencesRepository(context.filesDir.absolutePath)
        }
        single<ReadoutStartSoundPreferencesRepository> {
            createReadoutStartSoundPreferencesRepository(context.filesDir.absolutePath)
        }
        single<ThemePreferencesRepository> {
            createThemePreferencesRepository(context.filesDir.absolutePath)
        }
        single<ServerIpPreferencesRepository> {
            AndroidServerIpPreferencesRepository(context.serverIpDataStore)
        }
        single<ConsoleAddressPreferencesRepository> {
            createConsoleAddressPreferencesRepository(context.filesDir.absolutePath)
        }
        // ネットワーク（KoDriver サーバーのバージョン取得 / GitHub リリース確認）
        single<ServerVersionRepository> { HttpServerVersionRepository() }
        single<AppUpdateRepository> { GitHubAppReleaseRepository() }
        single<FeedbackSenderRepository> { SentryFeedbackSenderRepository() }
    }

/**
 * androidDataModule から分離したアプリ設定系バインド（画面スリープ抑止・Dynamic Color・
 * タップ時ハプティックフィードバック。LongMethod 対策）。
 */
private fun androidDataModuleAppSettings(context: Context) =
    module {
        // 画面スリープ抑止（Android は端末画面を実際に点灯維持）
        single<KeepScreenOnEnabledRepository> {
            AndroidKeepScreenOnEnabledRepository(context.keepScreenOnDataStore)
        }
        // Dynamic Color（Android 12+ の Material You 配色を使うかどうかの設定）
        single<DynamicColorEnabledRepository> {
            AndroidDynamicColorEnabledRepository(context.dynamicColorDataStore)
        }
        // タップ時ハプティックフィードバック（Android専用設定）
        single<HapticFeedbackEnabledRepository> {
            AndroidHapticFeedbackEnabledRepository(context.hapticFeedbackEnabledDataStore)
        }
        // 端末が振動ハードウェアを備えているか（設定画面での項目表示可否に使用）
        single<HapticFeedbackAvailabilityRepository> {
            AndroidHapticFeedbackAvailabilityRepository(context)
        }
        // ACCESS_LOCAL_NETWORK 権限（Android 17+で必要。未満は常に許可済み扱い）が許可されているか（設定画面でのバッジ表示に使用）
        single<AccessLocalNetworkPermissionRepository> {
            AndroidAccessLocalNetworkPermissionRepository(context)
        }
        // オーバーレイの文字サイズ設定
        single<OverlayTextSizePreferencesRepository> {
            createOverlayTextSizePreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイの文字色設定
        single<OverlayTextColorPreferencesRepository> {
            createOverlayTextColorPreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイの背景透明度設定
        single<OverlayBackgroundOpacityPreferencesRepository> {
            createOverlayBackgroundOpacityPreferencesRepository(context.filesDir.absolutePath)
        }
        single<WakeWordHotkeyPreferencesRepository> {
            createWakeWordHotkeyPreferencesRepository(context.filesDir.absolutePath)
        }
        single<MicrophoneCutQuietSoundPreferencesRepository> {
            createMicrophoneCutQuietSoundPreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイの表示ON/OFF設定
        single<OverlayVisiblePreferencesRepository> {
            createOverlayVisiblePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LowVolumeWarningEnabledPreferencesRepository> {
            createLowVolumeWarningEnabledPreferencesRepository(context.filesDir.absolutePath)
        }
        single<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository> {
            createManualReadoutPlayButtonLayoutPreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイウィンドウの位置・サイズ（デスクトップ版のみ使用するが、Koin の定義は両プラットフォームで揃える）
        single<OverlayWindowBoundsPreferencesRepository> {
            createOverlayWindowBoundsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<MainWindowBoundsPreferencesRepository> {
            createMainWindowBoundsPreferencesRepository(context.filesDir.absolutePath)
        }
    }

/**
 * androidDataModule から分離した共通の DataStore バインドと TelemetryLog（LongMethod 対策）。
 */
private fun androidDataModuleCommonPreferences(context: Context) =
    module {
        single<ReadoutStartSoundEnabledPreferencesRepository> {
            createReadoutStartSoundEnabledPreferencesRepository(context.filesDir.absolutePath)
        }
        single<FeedbackCooldownPreferencesRepository> {
            createFeedbackCooldownPreferencesRepository(context.filesDir.absolutePath)
        }
        // テレメトリログ（Room データベース）
        single<TelemetryLogRepository> {
            createTelemetryLogRepository(context = context)
        }
    }

/** GT7 向けの共通 DataStore Repository バインド。 */
private fun androidDataModuleCommonSimulatorPreferences(context: Context) =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath, Simulator.Gt7Ps5)
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createManualReadoutSetPreferencesRepository(context.filesDir.absolutePath, Simulator.Gt7Ps5)
        }
    }

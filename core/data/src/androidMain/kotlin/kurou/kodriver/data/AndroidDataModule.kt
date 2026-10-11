package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import kurou.kodriver.data.device.AndroidAccessLocalNetworkPermissionRepository
import kurou.kodriver.data.device.AndroidHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.preferences.AndroidDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.AndroidHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.AndroidKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.AndroidReadoutPreferencesRepository
import kurou.kodriver.data.preferences.AndroidServerIpPreferencesRepository
import kurou.kodriver.data.preferences.AndroidSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.createConsoleAddressPreferencesRepository
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createFeedbackCooldownPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayBackgroundOpacityPreferencesRepository
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
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.release.HttpServerVersionRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.data.websocket.createWebSocketHttpClient
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore("simulator_preferences")
private val Context.readoutDataStore by preferencesDataStore("readout_preferences")
private val Context.serverIpDataStore by preferencesDataStore("server_ip_preferences")
private val Context.keepScreenOnDataStore by preferencesDataStore("keep_screen_on_preferences")
private val Context.dynamicColorDataStore by preferencesDataStore("dynamic_color_preferences")
private val Context.hapticFeedbackEnabledDataStore by preferencesDataStore("haptic_feedback_enabled_preferences")

/**
 * Android 版の Repository バインドを行う Koin モジュール（:core:data / androidMain）。
 *
 * app エントリーポイント（androidApp）で composition root として束ねられ、各 feature モジュールの
 * UseCase が get() で解決する Repository 実装を提供する。デスクトップ版（DesktopDataModule）との違いは、
 * LMU の走行データを Windows 共有メモリではなく **KoDriver サーバーへの WebSocket** から取得する点。
 * 大半は DataStore バインドで、ServerVersion/AppUpdate はネットワーク、TelemetryLog は Room DB。
 */
fun androidDataModule(context: Context) =
    module {
        single<Context> { context }

        // 設定永続化（DataStore。ファイルは context.filesDir 配下）
        single<SimulatorPreferencesRepository> {
            AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
        }
        single<ReadoutPreferencesRepository> {
            AndroidReadoutPreferencesRepository(context.readoutDataStore)
        }
        single<QueuePreferencesRepository> {
            createQueuePreferencesRepository(context.filesDir.absolutePath)
        }
        // WebSocket 用 HttpClient は LMU・ACE の全リポジトリで単一インスタンスを共有する。
        single<HttpClient> { createWebSocketHttpClient() }
        includes(androidDataModuleLmuWindows(context))
        includes(androidDataModuleAceWindows(context))
        includes(androidDataModuleGt7Ps5(context))
        includes(androidDataModuleCommonPreferences(context))
        includes(androidDataModuleAppSettings(context))
        includes(androidDataModuleMisc(context))
    }

/**
 * androidDataModule から分離した各種 DataStore バインドとネットワーク系バインド（LongMethod 対策）。
 */
private fun androidDataModuleMisc(context: Context) =
    module {
        single<DebugStateCardOrderPreferencesRepository> {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath)
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
        // ACCESS_LOCAL_NETWORK 権限（Android 16+）が許可されているか（設定画面でのバッジ表示に使用）
        single<AccessLocalNetworkPermissionRepository> {
            AndroidAccessLocalNetworkPermissionRepository(context)
        }
        // オーバーレイの文字サイズ設定
        single<OverlayTextSizePreferencesRepository> {
            createOverlayTextSizePreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイの背景透明度設定
        single<OverlayBackgroundOpacityPreferencesRepository> {
            createOverlayBackgroundOpacityPreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイの表示ON/OFF設定
        single<OverlayVisiblePreferencesRepository> {
            createOverlayVisiblePreferencesRepository(context.filesDir.absolutePath)
        }
        // オーバーレイウィンドウの位置・サイズ（デスクトップ版のみ使用するが、Koin の定義は両プラットフォームで揃える）
        single<OverlayWindowBoundsPreferencesRepository> {
            createOverlayWindowBoundsPreferencesRepository(context.filesDir.absolutePath)
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

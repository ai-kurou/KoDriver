package kurou.kodriver.data

import kurou.kodriver.data.debugreadout.InMemoryDebugReadoutRequestRepository
import kurou.kodriver.data.device.JvmAccessLocalNetworkPermissionRepository
import kurou.kodriver.data.device.JvmHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutRequestRepository
import kurou.kodriver.data.preferences.JvmDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.JvmHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.JvmKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.createAndroidAudioUsagePreferencesRepository
import kurou.kodriver.data.preferences.createAutoReadoutPreferencesRepository
import kurou.kodriver.data.preferences.createConsoleAddressPreferencesRepository
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createFeedbackCooldownPreferencesRepository
import kurou.kodriver.data.preferences.createLowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createManualReadoutPlayButtonLayoutPreferencesRepository
import kurou.kodriver.data.preferences.createManualReadoutSetPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayTextColorPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayTextSizePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayVisiblePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayWindowBoundsPreferencesRepository
import kurou.kodriver.data.preferences.createQueuePreferencesRepository
import kurou.kodriver.data.preferences.createReadoutHotkeyPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundPreferencesRepository
import kurou.kodriver.data.preferences.createSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.createSoundVolumePreferencesRepository
import kurou.kodriver.data.preferences.createThemePreferencesRepository
import kurou.kodriver.data.preferences.createVoicePitchPreferencesRepository
import kurou.kodriver.data.preferences.createVoicePreferencesRepository
import kurou.kodriver.data.preferences.createVoiceSpeedPreferencesRepository
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * デスクトップ（JVM）版の Repository バインドを行う Koin モジュール（:core:data / jvmMain）。
 *
 * app エントリーポイント（desktopApp）で composition root として束ねられる。ここで束ねた
 * Repository 実装が、各 feature モジュールの UseCase から get() で解決される。
 * 大半は引数 `directory` 配下（Desktop アプリは `~/.kodriver`）の DataStore バインド。例外は AppUpdate（GitHub ネットワーク）・
 * KeepScreenOn（プラットフォーム実装）・TelemetryLog（Room DB）。Android 版は AndroidDataModule を参照。
 */
fun desktopDataModule(directory: String): Module =
    module {
        single {
            CoreAutoReadoutSettingsResetter(
                autoReadoutPreferencesRepository = get(),
                queuePreferencesRepository = get(),
                readoutStartSoundEnabledPreferencesRepository = get(),
            )
        } bind AutoReadoutSettingsResetter::class
        single<DebugReadoutRequestRepository> { InMemoryDebugReadoutRequestRepository() }
        single<ManualReadoutRequestRepository> { InMemoryManualReadoutRequestRepository() }
        single<ReadoutHotkeyPreferencesRepository> {
            createReadoutHotkeyPreferencesRepository(directory)
        }
        includes(desktopDataModuleCommonSimulatorPreferences(directory))
        includes(desktopDataModuleWindowsSimulatorPreferences(directory))
        // 設定永続化（DataStore。ファイルは directory 配下）
        single<SimulatorPreferencesRepository> {
            createSimulatorPreferencesRepository(directory = directory)
        }
        single<AutoReadoutPreferencesRepository> {
            createAutoReadoutPreferencesRepository(directory = directory)
        }
        single<QueuePreferencesRepository> {
            createQueuePreferencesRepository(directory = directory)
        }
        single<AndroidAudioUsagePreferencesRepository> {
            createAndroidAudioUsagePreferencesRepository(directory = directory)
        }
        single<ReadoutStartSoundEnabledPreferencesRepository> {
            createReadoutStartSoundEnabledPreferencesRepository(directory = directory)
        }
        single<SoundVolumePreferencesRepository> {
            createSoundVolumePreferencesRepository(directory = directory)
        }
        single<VoiceSpeedPreferencesRepository> {
            createVoiceSpeedPreferencesRepository(directory = directory)
        }
        single<VoicePitchPreferencesRepository> {
            createVoicePitchPreferencesRepository(directory = directory)
        }
        single<VoicePreferencesRepository> {
            createVoicePreferencesRepository(directory = directory)
        }
        single<ReadoutStartSoundPreferencesRepository> {
            createReadoutStartSoundPreferencesRepository(directory = directory)
        }
        single<ThemePreferencesRepository> {
            createThemePreferencesRepository(directory = directory)
        }
        single<OverlayTextSizePreferencesRepository> {
            createOverlayTextSizePreferencesRepository(directory = directory)
        }
        // オーバーレイの文字色設定
        single<OverlayTextColorPreferencesRepository> {
            createOverlayTextColorPreferencesRepository(directory = directory)
        }
        single<OverlayBackgroundOpacityPreferencesRepository> {
            createOverlayBackgroundOpacityPreferencesRepository(directory = directory)
        }
        single<OverlayVisiblePreferencesRepository> {
            createOverlayVisiblePreferencesRepository(directory = directory)
        }
        single<LowVolumeWarningEnabledPreferencesRepository> {
            createLowVolumeWarningEnabledPreferencesRepository(directory = directory)
        }
        single<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository> {
            createManualReadoutPlayButtonLayoutPreferencesRepository(directory = directory)
        }
        single<OverlayWindowBoundsPreferencesRepository> {
            createOverlayWindowBoundsPreferencesRepository(directory = directory)
        }
        single<ConsoleAddressPreferencesRepository> {
            createConsoleAddressPreferencesRepository(directory = directory)
        }
        single<FeedbackCooldownPreferencesRepository> {
            createFeedbackCooldownPreferencesRepository(directory = directory)
        }
        // アプリ更新確認（GitHub リリース API を叩くネットワーク実装）
        single<AppUpdateRepository> { GitHubAppReleaseRepository() }
        // 画面スリープ抑止（プラットフォーム固有実装。Desktop は no-op 相当）
        single<KeepScreenOnEnabledRepository> { JvmKeepScreenOnEnabledRepository() }
        // Dynamic Color（プラットフォーム固有実装。Desktop は no-op 相当。Android 12+ でのみ意味を持つ）
        single<DynamicColorEnabledRepository> { JvmDynamicColorEnabledRepository() }
        // タップ時ハプティックフィードバック（プラットフォーム固有実装。Desktop は no-op 相当。Android専用設定）
        single<HapticFeedbackEnabledRepository> { JvmHapticFeedbackEnabledRepository() }
        single<HapticFeedbackAvailabilityRepository> { JvmHapticFeedbackAvailabilityRepository() }
        // ACCESS_LOCAL_NETWORK 権限（プラットフォーム固有実装。Desktop はこの権限自体が存在しないため常に許可済み扱い）
        single<AccessLocalNetworkPermissionRepository> { JvmAccessLocalNetworkPermissionRepository() }
        // テレメトリログ（Room データベース）
        single<TelemetryLogRepository> {
            createTelemetryLogRepository(directory = directory)
        }
        single<FeedbackSenderRepository> { SentryFeedbackSenderRepository() }
    }

/** GT7 向けの共通 DataStore Repository バインド。 */
private fun desktopDataModuleCommonSimulatorPreferences(directory: String) =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createDebugStateCardOrderPreferencesRepository(directory, Simulator.Gt7Ps5)
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createManualReadoutSetPreferencesRepository(directory, Simulator.Gt7Ps5)
        }
    }

/** LMU・ACE 向けの共通 DataStore Repository バインド。 */
private fun desktopDataModuleWindowsSimulatorPreferences(directory: String) =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.LmuWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(
                directory = directory,
                simulator = Simulator.LmuWindows,
            )
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) {
            createManualReadoutSetPreferencesRepository(
                directory = directory,
                simulator = Simulator.LmuWindows,
            )
        }
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.AceWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(
                directory = directory,
                simulator = Simulator.AceWindows,
            )
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.AceWindows.id)) {
            createManualReadoutSetPreferencesRepository(
                directory = directory,
                simulator = Simulator.AceWindows,
            )
        }
    }

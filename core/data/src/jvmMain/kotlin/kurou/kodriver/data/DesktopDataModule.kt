package kurou.kodriver.data

import kurou.kodriver.data.device.JvmAccessLocalNetworkPermissionRepository
import kurou.kodriver.data.device.JvmHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.preferences.JvmDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.JvmHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.JvmKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.createConsoleAddressPreferencesRepository
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createFeedbackCooldownPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.data.preferences.createOverlayTextSizePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayVisiblePreferencesRepository
import kurou.kodriver.data.preferences.createOverlayWindowBoundsPreferencesRepository
import kurou.kodriver.data.preferences.createQueuePreferencesRepository
import kurou.kodriver.data.preferences.createReadoutPreferencesRepository
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
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.dsl.module

internal val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"

/**
 * デスクトップ（JVM）版の Repository バインドを行う Koin モジュール（:core:data / jvmMain）。
 *
 * app エントリーポイント（desktopApp）で composition root として束ねられる。ここで束ねた
 * Repository 実装が、各 feature モジュールの UseCase から get() で解決される。
 * 大半は `~/.kodriver` 配下の DataStore バインド。例外は AppUpdate（GitHub ネットワーク）・
 * KeepScreenOn（プラットフォーム実装）・TelemetryLog（Room DB）。Android 版は AndroidDataModule を参照。
 */
val desktopDataModule =
    module {
        includes(desktopDataModuleLmuWindows())
        includes(desktopDataModuleAceWindows())
        includes(desktopDataModuleGt7Ps5())
        // 設定永続化（DataStore。ファイルは ~/.kodriver 配下）
        single<SimulatorPreferencesRepository> {
            createSimulatorPreferencesRepository(directory = kodriverDirectory)
        }
        single<ReadoutPreferencesRepository> {
            createReadoutPreferencesRepository(directory = kodriverDirectory)
        }
        single<QueuePreferencesRepository> {
            createQueuePreferencesRepository(directory = kodriverDirectory)
        }
        single<ReadoutStartSoundEnabledPreferencesRepository> {
            createReadoutStartSoundEnabledPreferencesRepository(directory = kodriverDirectory)
        }
        single<DebugStateCardOrderPreferencesRepository> {
            createDebugStateCardOrderPreferencesRepository(directory = kodriverDirectory)
        }
        single<SoundVolumePreferencesRepository> {
            createSoundVolumePreferencesRepository(directory = kodriverDirectory)
        }
        single<VoiceSpeedPreferencesRepository> {
            createVoiceSpeedPreferencesRepository(directory = kodriverDirectory)
        }
        single<VoicePitchPreferencesRepository> {
            createVoicePitchPreferencesRepository(directory = kodriverDirectory)
        }
        single<VoicePreferencesRepository> {
            createVoicePreferencesRepository(directory = kodriverDirectory)
        }
        single<ReadoutStartSoundPreferencesRepository> {
            createReadoutStartSoundPreferencesRepository(directory = kodriverDirectory)
        }
        single<ThemePreferencesRepository> {
            createThemePreferencesRepository(directory = kodriverDirectory)
        }
        single<OverlayTextSizePreferencesRepository> {
            createOverlayTextSizePreferencesRepository(directory = kodriverDirectory)
        }
        single<OverlayBackgroundOpacityPreferencesRepository> {
            createOverlayBackgroundOpacityPreferencesRepository(directory = kodriverDirectory)
        }
        single<OverlayVisiblePreferencesRepository> {
            createOverlayVisiblePreferencesRepository(directory = kodriverDirectory)
        }
        single<OverlayWindowBoundsPreferencesRepository> {
            createOverlayWindowBoundsPreferencesRepository(directory = kodriverDirectory)
        }
        single<ConsoleAddressPreferencesRepository> {
            createConsoleAddressPreferencesRepository(directory = kodriverDirectory)
        }
        single<FeedbackCooldownPreferencesRepository> {
            createFeedbackCooldownPreferencesRepository(directory = kodriverDirectory)
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
            createTelemetryLogRepository(directory = kodriverDirectory)
        }
        single<FeedbackSenderRepository> { SentryFeedbackSenderRepository() }
    }

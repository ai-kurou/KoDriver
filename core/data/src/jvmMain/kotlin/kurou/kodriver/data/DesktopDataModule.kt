package kurou.kodriver.data

import kurou.kodriver.data.debugreadout.InMemoryDebugReadoutRequestRepository
import kurou.kodriver.data.device.JvmAccessLocalNetworkPermissionRepository
import kurou.kodriver.data.device.JvmHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutItemRequestRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutItemSpokenRepository
import kurou.kodriver.data.manualreadout.InMemoryManualReadoutRequestRepository
import kurou.kodriver.data.manualreadout.InMemorySpeechPlaybackStateRepository
import kurou.kodriver.data.manualreadout.InMemorySpeechRecognitionTranscriptRepository
import kurou.kodriver.data.preferences.JvmDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.JvmHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.JvmKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.createAndroidAudioUsagePreferencesRepository
import kurou.kodriver.data.preferences.createAutoReadoutPreferencesRepository
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
import kurou.kodriver.data.preferences.createReadoutHotkeyPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundPreferencesRepository
import kurou.kodriver.data.preferences.createSelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.data.preferences.createSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.createSoundVolumePreferencesRepository
import kurou.kodriver.data.preferences.createThemePreferencesRepository
import kurou.kodriver.data.preferences.createVoicePitchPreferencesRepository
import kurou.kodriver.data.preferences.createVoicePreferencesRepository
import kurou.kodriver.data.preferences.createVoiceSpeedPreferencesRepository
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.domain.exportimport.Exportable
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
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutItemRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSettingsResetter
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kurou.kodriver.domain.repository.SpeechRecognitionTranscriptRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * デスクトップ（JVM）版の Repository バインドを行う Koin モジュール（:core:data / jvmMain）。
 *
 * app エントリーポイント（desktopApp）で composition root として束ねられる。ここで束ねた
 * Repository 実装が、各 feature モジュールの UseCase から get() で解決される。
 * 大半は引数 `directory` 配下（Desktop アプリは `~/.kodriver`）の DataStore バインド。例外は AppUpdate（GitHub ネットワーク）・
 * KeepScreenOn（プラットフォーム実装）・TelemetryLog（Room DB）。Android 版は AndroidDataModule を参照。
 */
public fun desktopDataModule(directory: String): Module =
    module {
        includes(desktopDataModuleResetters())
        includes(desktopDataModuleInMemoryRepositories())
        includes(desktopDataModuleWindowBounds(directory))
        includes(desktopDataModuleCommonSimulatorPreferences(directory))
        includes(desktopDataModuleWindowsSimulatorPreferences(directory))
        includes(desktopDataModuleAudioPreferences(directory))
        includes(desktopDataModuleAppPreferences(directory))
        includes(desktopDataModulePlatform(directory))
    }

/** 設定リセット（Core 側の Resetter 実装を、各種 Repository を get() で解決して束ねる）。 */
private fun desktopDataModuleResetters() =
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
    }

/** メモリ上のみで保持する手動読み上げ・デバッグ・音声認識の状態 Repository バインド（永続化しない）。 */
private fun desktopDataModuleInMemoryRepositories() =
    module {
        single<DebugReadoutRequestRepository> { InMemoryDebugReadoutRequestRepository() }
        single<ManualReadoutRequestRepository> { InMemoryManualReadoutRequestRepository() }
        single<ManualReadoutItemRequestRepository> { InMemoryManualReadoutItemRequestRepository() }
        single<ManualReadoutItemSpokenRepository> { InMemoryManualReadoutItemSpokenRepository() }
        single<SpeechPlaybackStateRepository> { InMemorySpeechPlaybackStateRepository() }
        single<SpeechRecognitionTranscriptRepository> { InMemorySpeechRecognitionTranscriptRepository() }
    }

/** 音声・読み上げ関連の DataStore Repository バインド（ファイルは directory 配下）。 */
private fun desktopDataModuleAudioPreferences(directory: String) =
    module {
        single {
            createReadoutHotkeyPreferencesRepository(directory)
        } binds arrayOf(ReadoutHotkeyPreferencesRepository::class, Exportable::class)
        single {
            createMicrophoneCutQuietSoundPreferencesRepository(directory = directory)
        } binds arrayOf(MicrophoneCutQuietSoundPreferencesRepository::class, Exportable::class)
        // 設定永続化（DataStore。ファイルは directory 配下）
        single<AutoReadoutPreferencesRepository> {
            createAutoReadoutPreferencesRepository(directory = directory)
        }
        single {
            createQueuePreferencesRepository(directory = directory)
        } binds arrayOf(QueuePreferencesRepository::class, Exportable::class)
        single {
            createAndroidAudioUsagePreferencesRepository(directory = directory)
        } binds arrayOf(AndroidAudioUsagePreferencesRepository::class, Exportable::class)
        single {
            createReadoutStartSoundEnabledPreferencesRepository(directory = directory)
        } binds arrayOf(ReadoutStartSoundEnabledPreferencesRepository::class, Exportable::class)
        single {
            createSoundVolumePreferencesRepository(directory = directory)
        } binds arrayOf(SoundVolumePreferencesRepository::class, Exportable::class)
        single {
            createVoiceSpeedPreferencesRepository(directory = directory)
        } binds arrayOf(VoiceSpeedPreferencesRepository::class, Exportable::class)
        single {
            createVoicePitchPreferencesRepository(directory = directory)
        } binds arrayOf(VoicePitchPreferencesRepository::class, Exportable::class)
        // 選択したマイク（音声入力デバイス）名
        single<SelectedAudioInputDevicePreferencesRepository> {
            createSelectedAudioInputDevicePreferencesRepository(directory = directory)
        }
        single<VoicePreferencesRepository> {
            createVoicePreferencesRepository(directory = directory)
        }
        single {
            createReadoutStartSoundPreferencesRepository(directory = directory)
        } binds arrayOf(ReadoutStartSoundPreferencesRepository::class, Exportable::class)
    }

/** アプリ全体・オーバーレイ・接続先などの DataStore Repository バインド（ファイルは directory 配下）。 */
private fun desktopDataModuleAppPreferences(directory: String) =
    module {
        single<SimulatorPreferencesRepository> {
            createSimulatorPreferencesRepository(directory = directory)
        }
        single {
            createThemePreferencesRepository(directory = directory)
        } binds arrayOf(ThemePreferencesRepository::class, Exportable::class)
        single {
            createOverlayTextSizePreferencesRepository(directory = directory)
        } binds arrayOf(OverlayTextSizePreferencesRepository::class, Exportable::class)
        // オーバーレイの文字色設定
        single {
            createOverlayTextColorPreferencesRepository(directory = directory)
        } binds arrayOf(OverlayTextColorPreferencesRepository::class, Exportable::class)
        single {
            createOverlayBackgroundOpacityPreferencesRepository(directory = directory)
        } binds arrayOf(OverlayBackgroundOpacityPreferencesRepository::class, Exportable::class)
        single {
            createOverlayVisiblePreferencesRepository(directory = directory)
        } binds arrayOf(OverlayVisiblePreferencesRepository::class, Exportable::class)
        single {
            createLowVolumeWarningEnabledPreferencesRepository(directory = directory)
        } binds arrayOf(LowVolumeWarningEnabledPreferencesRepository::class, Exportable::class)
        single {
            createManualReadoutPlayButtonLayoutPreferencesRepository(directory = directory)
        } binds arrayOf(ManualReadoutPlayButtonLayoutEnabledPreferencesRepository::class, Exportable::class)
        single<ConsoleAddressPreferencesRepository> {
            createConsoleAddressPreferencesRepository(directory = directory)
        }
        single<FeedbackCooldownPreferencesRepository> {
            createFeedbackCooldownPreferencesRepository(directory = directory)
        }
    }

/** プラットフォーム依存・ネットワーク・ログ・フィードバック送信の Repository バインド。 */
private fun desktopDataModulePlatform(directory: String) =
    module {
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

/** オーバーレイ・メインウィンドウの位置・サイズの DataStore Repository バインド。 */
private fun desktopDataModuleWindowBounds(directory: String) =
    module {
        single<OverlayWindowBoundsPreferencesRepository> {
            createOverlayWindowBoundsPreferencesRepository(directory = directory)
        }
        single<MainWindowBoundsPreferencesRepository> {
            createMainWindowBoundsPreferencesRepository(directory = directory)
        }
    }

/** GT7 向けの共通 DataStore Repository バインド。 */
private fun desktopDataModuleCommonSimulatorPreferences(directory: String) =
    module {
        single(named(Simulator.Gt7Ps5.id)) {
            createDebugStateCardOrderPreferencesRepository(directory, Simulator.Gt7Ps5)
        } binds arrayOf(DebugStateCardOrderPreferencesRepository::class, Exportable::class)
        single(named(Simulator.Gt7Ps5.id)) {
            createManualReadoutSetPreferencesRepository(directory, Simulator.Gt7Ps5)
        } binds arrayOf(ManualReadoutSetPreferencesRepository::class, Exportable::class)
    }

/** LMU・ACE 向けの共通 DataStore Repository バインド。 */
private fun desktopDataModuleWindowsSimulatorPreferences(directory: String) =
    module {
        single(named(Simulator.LmuWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(
                directory = directory,
                simulator = Simulator.LmuWindows,
            )
        } binds arrayOf(DebugStateCardOrderPreferencesRepository::class, Exportable::class)
        single(named(Simulator.LmuWindows.id)) {
            createManualReadoutSetPreferencesRepository(
                directory = directory,
                simulator = Simulator.LmuWindows,
            )
        } binds arrayOf(ManualReadoutSetPreferencesRepository::class, Exportable::class)
        single(named(Simulator.AceWindows.id)) {
            createDebugStateCardOrderPreferencesRepository(
                directory = directory,
                simulator = Simulator.AceWindows,
            )
        } binds arrayOf(DebugStateCardOrderPreferencesRepository::class, Exportable::class)
        single(named(Simulator.AceWindows.id)) {
            createManualReadoutSetPreferencesRepository(
                directory = directory,
                simulator = Simulator.AceWindows,
            )
        } binds arrayOf(ManualReadoutSetPreferencesRepository::class, Exportable::class)
    }

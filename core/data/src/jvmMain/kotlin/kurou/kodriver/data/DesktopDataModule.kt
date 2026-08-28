package kurou.kodriver.data

import kurou.kodriver.data.device.JvmHapticFeedbackAvailabilityRepository
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.preferences.JvmDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.JvmHapticFeedbackEnabledRepository
import kurou.kodriver.data.preferences.JvmKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.data.preferences.createConsoleAddressPreferencesRepository
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createLmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.data.preferences.createQueuePreferencesRepository
import kurou.kodriver.data.preferences.createReadoutPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.data.preferences.createReadoutStartSoundPreferencesRepository
import kurou.kodriver.data.preferences.createSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.createSoundVolumePreferencesRepository
import kurou.kodriver.data.preferences.createThemePreferencesRepository
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import org.koin.dsl.module

private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"

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
        single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
            createGt7Ps5RemainingFuelLapsPreferencesRepository(kodriverDirectory)
        }
        single<Gt7Ps5RemainingFuelPreferencesRepository> {
            createGt7Ps5RemainingFuelPreferencesRepository(kodriverDirectory)
        }
        single<Gt7Ps5TyreTemperaturePreferencesRepository> {
            createGt7Ps5TyreTemperaturePreferencesRepository(kodriverDirectory)
        }
        single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(directory = kodriverDirectory)
        }
        single<DebugStateCardOrderPreferencesRepository> {
            createDebugStateCardOrderPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsFlagPreferencesRepository> {
            createLmuWindowsFlagPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleApproachPreferencesRepository> {
            createLmuWindowsVehicleApproachPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleDamagePreferencesRepository> {
            createLmuWindowsVehicleDamagePreferencesRepository(directory = kodriverDirectory)
        }
        single<SoundVolumePreferencesRepository> {
            createSoundVolumePreferencesRepository(directory = kodriverDirectory)
        }
        single<ReadoutStartSoundPreferencesRepository> {
            createReadoutStartSoundPreferencesRepository(directory = kodriverDirectory)
        }
        single<ThemePreferencesRepository> {
            createThemePreferencesRepository(directory = kodriverDirectory)
        }
        single<Gt7Ps5MyBestLapPreferencesRepository> {
            createGt7Ps5MyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsMyBestLapPreferencesRepository> {
            createLmuWindowsMyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsMyBestLapPreferencesRepository> {
            createAceWindowsMyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsRedFlagPreferencesRepository> {
            createLmuWindowsRedFlagPreferencesRepository(directory = kodriverDirectory)
        }
        single<ConsoleAddressPreferencesRepository> {
            createConsoleAddressPreferencesRepository(directory = kodriverDirectory)
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
        single<LmuWindowsTyreTemperaturePreferencesRepository> {
            createLmuWindowsTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsTyreWearPreferencesRepository> {
            createLmuWindowsTyreWearPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsRemainingVirtualEnergyPreferencesRepository> {
            createLmuWindowsRemainingVirtualEnergyPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsRemainingFuelPreferencesRepository> {
            createAceWindowsRemainingFuelPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsFlagPreferencesRepository> {
            createAceWindowsFlagPreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsTyreTemperaturePreferencesRepository> {
            createAceWindowsTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
        }
        single<AceWindowsVehicleApproachPreferencesRepository> {
            createAceWindowsVehicleApproachPreferencesRepository(directory = kodriverDirectory)
        }
        single<LmuWindowsPitTimingPreferencesRepository> {
            createLmuWindowsPitTimingPreferencesRepository(directory = kodriverDirectory)
        }
        // テレメトリログ（Room データベース）
        single<TelemetryLogRepository> {
            createTelemetryLogRepository(directory = kodriverDirectory)
        }
        single<FeedbackSenderRepository> { SentryFeedbackSenderRepository() }
    }

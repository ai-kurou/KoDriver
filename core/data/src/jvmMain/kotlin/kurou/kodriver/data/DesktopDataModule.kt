package kurou.kodriver.data

import kurou.kodriver.data.repository.LmuWindowsMyBestLapEnabledRepositoryImpl
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
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
val desktopDataModule = module {
    // 設定永続化（DataStore。ファイルは ~/.kodriver 配下）
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutPreferencesRepository> {
        createReadoutPreferencesRepository(directory = kodriverDirectory)
    }
    single<LmuWindowsMyBestLapEnabledRepository> {
        LmuWindowsMyBestLapEnabledRepositoryImpl(get())
    }
    single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
        createGt7Ps5RemainingFuelLapsPreferencesRepository(kodriverDirectory)
    }
    single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
        createLmuWindowsVehicleApproachThresholdsPreferencesRepository(directory = kodriverDirectory)
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
    single<ConsoleAddressPreferencesRepository> {
        createConsoleAddressPreferencesRepository(directory = kodriverDirectory)
    }
    // アプリ更新確認（GitHub リリース API を叩くネットワーク実装）
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
    // 画面スリープ抑止（プラットフォーム固有実装。Desktop は no-op 相当）
    single<KeepScreenOnEnabledRepository> { JvmKeepScreenOnEnabledRepository() }
    // 設定永続化（DataStore）
    single<ExitConfirmationEnabledRepository> {
        createExitConfirmationEnabledRepository(directory = kodriverDirectory)
    }
    single<LmuWindowsTyreTemperaturePreferencesRepository> {
        createLmuWindowsTyreTemperaturePreferencesRepository(directory = kodriverDirectory)
    }
    // テレメトリログ（Room データベース）
    single<TelemetryLogRepository> {
        createTelemetryLogRepository(directory = kodriverDirectory)
    }
}

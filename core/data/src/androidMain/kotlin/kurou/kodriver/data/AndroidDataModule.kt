package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore("simulator_preferences")
private val Context.readoutDataStore by preferencesDataStore("readout_preferences")
private val Context.serverIpDataStore by preferencesDataStore("server_ip_preferences")
private val Context.keepScreenOnDataStore by preferencesDataStore("keep_screen_on_preferences")
private val Context.exitConfirmationDataStore by preferencesDataStore("exit_confirmation_preferences")

/**
 * Android 版の Repository バインドを行う Koin モジュール（:core:data / androidMain）。
 *
 * app エントリーポイント（androidApp）で composition root として束ねられ、各 feature モジュールの
 * UseCase が get() で解決する Repository 実装を提供する。デスクトップ版（DesktopDataModule）との違いは、
 * LMU の走行データを Windows 共有メモリではなく **KoDriver サーバーへの WebSocket** から取得する点。
 * 大半は DataStore バインドで、ServerVersion/AppUpdate はネットワーク、TelemetryLog は Room DB。
 */
fun androidDataModule(context: Context) = module {
    single<Context> { context }

    // 設定永続化（DataStore。ファイルは context.filesDir 配下）
    single<SimulatorPreferencesRepository> {
        AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
    }
    single<ReadoutPreferencesRepository> {
        AndroidReadoutPreferencesRepository(context.readoutDataStore)
    }
    single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
        createGt7Ps5RemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
    }
    // LMU 走行データの取得元（Android は KoDriver サーバーへの WebSocket クライアント実装）
    single<LmuWindowsRepository> { WebSocketLmuWindowsRepository(get()) }
    single<LmuWindowsFlagRepository> { WebSocketLmuWindowsFlagRepository(get()) }
    single<LmuWindowsVehicleApproachRepository> { WebSocketLmuWindowsVehicleApproachRepository(get()) }
    single<LmuWindowsVehicleDamageRepository> { WebSocketLmuWindowsVehicleDamageRepository(get()) }
    single<LmuWindowsTyreCarcassTemperatureRepository> { WebSocketLmuWindowsTyreCarcassTemperatureRepository(get()) }
    single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
        createLmuWindowsVehicleApproachThresholdsPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsFlagPreferencesRepository> {
        createLmuWindowsFlagPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsVehicleApproachPreferencesRepository> {
        createLmuWindowsVehicleApproachPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsVehicleDamagePreferencesRepository> {
        createLmuWindowsVehicleDamagePreferencesRepository(context.filesDir.absolutePath)
    }
    single<SoundVolumePreferencesRepository> {
        createSoundVolumePreferencesRepository(context.filesDir.absolutePath)
    }
    single<ReadoutStartSoundPreferencesRepository> {
        createReadoutStartSoundPreferencesRepository(context.filesDir.absolutePath)
    }
    single<ThemePreferencesRepository> {
        createThemePreferencesRepository(context.filesDir.absolutePath)
    }
    single<Gt7Ps5MyBestLapPreferencesRepository> {
        createGt7Ps5MyBestLapPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsMyBestLapPreferencesRepository> {
        createLmuWindowsMyBestLapPreferencesRepository(context.filesDir.absolutePath)
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
    // 画面スリープ抑止（Android は端末画面を実際に点灯維持）
    single<KeepScreenOnEnabledRepository> {
        AndroidKeepScreenOnEnabledRepository(context.keepScreenOnDataStore)
    }
    single<ExitConfirmationEnabledRepository> {
        AndroidExitConfirmationEnabledRepository(context.exitConfirmationDataStore)
    }
    single<LmuWindowsTyreTemperaturePreferencesRepository> {
        createLmuWindowsTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
    }
    // テレメトリログ（Room データベース）
    single<TelemetryLogRepository> {
        createTelemetryLogRepository(context = context)
    }
}

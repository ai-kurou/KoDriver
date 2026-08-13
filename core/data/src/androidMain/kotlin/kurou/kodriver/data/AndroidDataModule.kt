package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import kurou.kodriver.data.feedback.SentryFeedbackSenderRepository
import kurou.kodriver.data.preferences.AndroidDynamicColorEnabledRepository
import kurou.kodriver.data.preferences.AndroidKeepScreenOnEnabledRepository
import kurou.kodriver.data.preferences.AndroidReadoutPreferencesRepository
import kurou.kodriver.data.preferences.AndroidServerIpPreferencesRepository
import kurou.kodriver.data.preferences.AndroidSimulatorPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsFlagPreferencesRepository
import kurou.kodriver.data.preferences.createAceWindowsRemainingFuelPreferencesRepository
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
import kurou.kodriver.data.preferences.createReadoutStartSoundPreferencesRepository
import kurou.kodriver.data.preferences.createSoundVolumePreferencesRepository
import kurou.kodriver.data.preferences.createThemePreferencesRepository
import kurou.kodriver.data.release.GitHubAppReleaseRepository
import kurou.kodriver.data.release.HttpServerVersionRepository
import kurou.kodriver.data.telemetrylog.createTelemetryLogRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsFlagRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsFuelRepository
import kurou.kodriver.data.websocket.WebSocketAceWindowsStatusRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsFlagRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsPitStatusRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsTyreWearRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleApproachRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleClassRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVehicleDamageRepository
import kurou.kodriver.data.websocket.WebSocketLmuWindowsVirtualEnergyRepository
import kurou.kodriver.data.websocket.createWebSocketHttpClient
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
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
private val Context.dynamicColorDataStore by preferencesDataStore("dynamic_color_preferences")

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
        single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
            createGt7Ps5RemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<Gt7Ps5RemainingFuelPreferencesRepository> {
            createGt7Ps5RemainingFuelPreferencesRepository(context.filesDir.absolutePath)
        }
        single<Gt7Ps5TyreTemperaturePreferencesRepository> {
            createGt7Ps5TyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        // LMU 走行データの取得元（Android は KoDriver サーバーへの WebSocket クライアント実装）。
        // HttpClient は全リポジトリで単一インスタンスを共有する。
        single<HttpClient> { createWebSocketHttpClient() }
        single<LmuWindowsRepository> { WebSocketLmuWindowsRepository(serverIpRepository = get(), client = get()) }
        single<LmuWindowsFlagRepository> {
            WebSocketLmuWindowsFlagRepository(
                serverIpRepository = get(),
                client = get(),
            )
        }
        single<LmuWindowsVehicleApproachRepository> {
            WebSocketLmuWindowsVehicleApproachRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVehicleDamageRepository> {
            WebSocketLmuWindowsVehicleDamageRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsTyreCarcassTemperatureRepository> {
            WebSocketLmuWindowsTyreCarcassTemperatureRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVehicleClassRepository> {
            WebSocketLmuWindowsVehicleClassRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsTyreWearRepository> {
            WebSocketLmuWindowsTyreWearRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVirtualEnergyRepository> {
            WebSocketLmuWindowsVirtualEnergyRepository(serverIpRepository = get(), client = get())
        }
        single<LmuWindowsVehicleApproachThresholdsPreferencesRepository> {
            createLmuWindowsVehicleApproachThresholdsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<DebugStateCardOrderPreferencesRepository> {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath)
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
        single<LmuWindowsRedFlagPreferencesRepository> {
            createLmuWindowsRedFlagPreferencesRepository(context.filesDir.absolutePath)
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
        // Dynamic Color（Android 12+ の Material You 配色を使うかどうかの設定）
        single<DynamicColorEnabledRepository> {
            AndroidDynamicColorEnabledRepository(context.dynamicColorDataStore)
        }
        single<FeedbackSenderRepository> { SentryFeedbackSenderRepository() }
        includes(androidDataModuleAceWindows())
        includes(androidDataModuleThresholdPreferences(context))
        includes(androidDataModuleLmuWindowsPitStatus())
    }

/**
 * androidDataModule から分離した LMU のピット状態取得用バインド（LongMethod 対策）。
 */
private fun androidDataModuleLmuWindowsPitStatus() =
    module {
        single<LmuWindowsPitStatusRepository> {
            WebSocketLmuWindowsPitStatusRepository(serverIpRepository = get(), client = get())
        }
    }

/**
 * androidDataModule から分離した ACE (Assetto Corsa EVO) の走行データ取得用バインド（LongMethod 対策）。
 *
 * ACE の走行データは Windows 共有メモリ専用実装のみのため、Android は LMU 系と同様に
 * KoDriver サーバーへの WebSocket クライアント実装を使う。
 */
private fun androidDataModuleAceWindows() =
    module {
        single<AceWindowsFuelRepository> {
            WebSocketAceWindowsFuelRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsFlagRepository> {
            WebSocketAceWindowsFlagRepository(serverIpRepository = get(), client = get())
        }
        single<AceWindowsStatusRepository> {
            WebSocketAceWindowsStatusRepository(serverIpRepository = get(), client = get())
        }
    }

/**
 * androidDataModule から分離した閾値系 DataStore バインドと TelemetryLog（LongMethod 対策）。
 */
private fun androidDataModuleThresholdPreferences(context: Context) =
    module {
        single<LmuWindowsTyreTemperaturePreferencesRepository> {
            createLmuWindowsTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository> {
            createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsTyreWearPreferencesRepository> {
            createLmuWindowsTyreWearPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsRemainingVirtualEnergyPreferencesRepository> {
            createLmuWindowsRemainingVirtualEnergyPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsRemainingFuelPreferencesRepository> {
            createAceWindowsRemainingFuelPreferencesRepository(context.filesDir.absolutePath)
        }
        single<AceWindowsFlagPreferencesRepository> {
            createAceWindowsFlagPreferencesRepository(context.filesDir.absolutePath)
        }
        single<LmuWindowsPitTimingPreferencesRepository> {
            createLmuWindowsPitTimingPreferencesRepository(context.filesDir.absolutePath)
        }
        // テレメトリログ（Room データベース）
        single<TelemetryLogRepository> {
            createTelemetryLogRepository(context = context)
        }
    }

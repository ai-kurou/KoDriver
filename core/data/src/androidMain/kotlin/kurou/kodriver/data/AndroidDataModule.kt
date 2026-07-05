package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl
import kurou.kodriver.data.repository.LmuWindowsMyBestLapEnabledRepositoryImpl
import kurou.kodriver.data.repository.LmuWindowsTyreTemperatureEnabledRepositoryImpl
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore("simulator_preferences")
private val Context.readoutDataStore by preferencesDataStore("readout_preferences")
private val Context.serverIpDataStore by preferencesDataStore("server_ip_preferences")
private val Context.keepScreenOnDataStore by preferencesDataStore("keep_screen_on_preferences")
private val Context.exitConfirmationDataStore by preferencesDataStore("exit_confirmation_preferences")

fun androidDataModule(context: Context) = module {
    single<Context> { context }
    single<SimulatorPreferencesRepository> {
        AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
    }
    single<ReadoutPreferencesRepository> {
        AndroidReadoutPreferencesRepository(context.readoutDataStore)
    }
    single<Gt7Ps5RemainingFuelLapsEnabledRepository> {
        Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl(get())
    }
    single<LmuWindowsMyBestLapEnabledRepository> {
        LmuWindowsMyBestLapEnabledRepositoryImpl(get())
    }
    single<LmuWindowsTyreTemperatureEnabledRepository> {
        LmuWindowsTyreTemperatureEnabledRepositoryImpl(get())
    }
    single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
        createGt7Ps5RemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsRepository> { EmptyLmuWindowsRepository() }
    single<FlagRepository> { WebSocketFlagRepository(get()) }
    single<ProximityRepository> { WebSocketProximityRepository(get()) }
    single<VehicleDamageRepository> { WebSocketVehicleDamageRepository(get()) }
    single<TyreCarcassTemperatureRepository> { EmptyTyreCarcassTemperatureRepository() }
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
    single<ServerIpRepository> {
        AndroidServerIpRepository(context.serverIpDataStore)
    }
    single<ConsoleAddressRepository> {
        createConsoleAddressRepository(context.filesDir.absolutePath)
    }
    single<ServerVersionRepository> { HttpServerVersionRepository() }
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
    single<KeepScreenOnPreferencesRepository> {
        AndroidKeepScreenOnPreferencesRepository(context.keepScreenOnDataStore)
    }
    single<ExitConfirmationPreferencesRepository> {
        AndroidExitConfirmationPreferencesRepository(context.exitConfirmationDataStore)
    }
    single<TelemetryLogRepository> {
        createTelemetryLogRepository(context = context)
    }
}

package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl
import kurou.kodriver.data.repository.LmuWindowsMyBestLapEnabledRepositoryImpl
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.ServerIpRepository
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
    single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
        createGt7Ps5RemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
    }
    single<LmuWindowsRepository> { EmptyLmuWindowsRepository() }
    single<LmuWindowsFlagRepository> { WebSocketLmuWindowsFlagRepository(get()) }
    single<LmuWindowsProximityRepository> { WebSocketLmuWindowsProximityRepository(get()) }
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
    single<ServerIpRepository> {
        AndroidServerIpRepository(context.serverIpDataStore)
    }
    single<ConsoleAddressRepository> {
        createConsoleAddressRepository(context.filesDir.absolutePath)
    }
    single<ServerVersionRepository> { HttpServerVersionRepository() }
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
    single<KeepScreenOnEnabledRepository> {
        AndroidKeepScreenOnEnabledRepository(context.keepScreenOnDataStore)
    }
    single<ExitConfirmationEnabledRepository> {
        AndroidExitConfirmationEnabledRepository(context.exitConfirmationDataStore)
    }
    single<LmuWindowsTyreTemperaturePreferencesRepository> {
        createLmuWindowsTyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
    }
    single<TelemetryLogRepository> {
        createTelemetryLogRepository(context = context)
    }
}

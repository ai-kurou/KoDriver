package kurou.kodriver.data

import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl
import kurou.kodriver.data.repository.LmuWindowsMyBestLapEnabledRepositoryImpl
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
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
val desktopDataModule = module {
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutPreferencesRepository> {
        createReadoutPreferencesRepository(directory = kodriverDirectory)
    }
    single<Gt7Ps5RemainingFuelLapsEnabledRepository> {
        Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl(get())
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
    single<ConsoleAddressRepository> {
        createConsoleAddressRepository(directory = kodriverDirectory)
    }
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
    single<KeepScreenOnPreferencesRepository> { JvmKeepScreenOnPreferencesRepository() }
    single<ExitConfirmationPreferencesRepository> {
        createExitConfirmationPreferencesRepository(directory = kodriverDirectory)
    }
    single<TelemetryLogRepository> {
        createTelemetryLogRepository(directory = kodriverDirectory)
    }
}

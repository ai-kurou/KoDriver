package kurou.kodriver.data

import kurou.kodriver.data.repository.Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ProximityThresholdsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
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
    single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
        createGt7Ps5RemainingFuelLapsPreferencesRepository(kodriverDirectory)
    }
    single<ProximityThresholdsPreferencesRepository> {
        createProximityThresholdsPreferencesRepository(directory = kodriverDirectory)
    }
    single<FlagPreferencesRepository> {
        createFlagPreferencesRepository(directory = kodriverDirectory)
    }
    single<VehicleApproachPreferencesRepository> {
        createVehicleApproachPreferencesRepository(directory = kodriverDirectory)
    }
    single<VehicleDamagePreferencesRepository> {
        createVehicleDamagePreferencesRepository(directory = kodriverDirectory)
    }
    single<SoundVolumePreferencesRepository> {
        createSoundVolumePreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutStartSoundPreferencesRepository> {
        createReadoutStartSoundPreferencesRepository(directory = kodriverDirectory)
    }
    single<MyBestLapPreferencesRepository> {
        createMyBestLapPreferencesRepository(directory = kodriverDirectory)
    }
    single<ConsoleAddressRepository> {
        createConsoleAddressRepository(directory = kodriverDirectory)
    }
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
    single<KeepScreenOnPreferencesRepository> { JvmKeepScreenOnPreferencesRepository() }
}

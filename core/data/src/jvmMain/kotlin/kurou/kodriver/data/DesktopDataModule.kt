package kurou.kodriver.data

import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumeRepository
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
    single<ProximityThresholdsRepository> {
        createProximityThresholdsRepository(directory = kodriverDirectory)
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
    single<SoundVolumeRepository> {
        createSoundVolumeRepository(directory = kodriverDirectory)
    }
    single<ReadoutStartSoundRepository> {
        createReadoutStartSoundRepository(directory = kodriverDirectory)
    }
    single<AppUpdateRepository> { GitHubAppReleaseRepository() }
}

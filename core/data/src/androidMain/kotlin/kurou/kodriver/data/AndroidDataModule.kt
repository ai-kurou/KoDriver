package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumeRepository
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore("simulator_preferences")
private val Context.readoutDataStore by preferencesDataStore("readout_preferences")

fun androidDataModule(context: Context) = module {
    single<Context> { context }
    single<SimulatorPreferencesRepository> {
        AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
    }
    single<ReadoutPreferencesRepository> {
        AndroidReadoutPreferencesRepository(context.readoutDataStore)
    }
    single<LmuRepository> { EmptyLmuRepository() }
    single<FlagRepository> { EmptyFlagRepository() }
    single<LmuRepository> { EmptyLmuRepository() }
    single<ProximityRepository> { EmptyProximityRepository() }
    single<VehicleDamageRepository> { EmptyVehicleDamageRepository() }
    single<ProximityThresholdsRepository> {
        createProximityThresholdsRepository(context.filesDir.absolutePath)
    }
    single<FlagPreferencesRepository> {
        createFlagPreferencesRepository(context.filesDir.absolutePath)
    }
    single<VehicleApproachPreferencesRepository> {
        createVehicleApproachPreferencesRepository(context.filesDir.absolutePath)
    }
    single<VehicleDamagePreferencesRepository> {
        createVehicleDamagePreferencesRepository(context.filesDir.absolutePath)
    }
    single<SoundVolumeRepository> {
        createSoundVolumeRepository(context.filesDir.absolutePath)
    }
}

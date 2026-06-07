package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
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
    single<ProximityRepository> { EmptyProximityRepository() }
    single<ProximityThresholdsRepository> {
        createProximityThresholdsRepository(context.filesDir.absolutePath)
    }
}

package kurou.kodriver.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.tts.AndroidTtsEngine
import org.koin.dsl.module

private val Context.simulatorDataStore by preferencesDataStore("simulator_preferences")
private val Context.readoutDataStore by preferencesDataStore("readout_preferences")

internal fun androidDataModule(context: Context) = module {
    single<SimulatorPreferencesRepository> {
        AndroidSimulatorPreferencesRepository(context.simulatorDataStore)
    }
    single<ReadoutPreferencesRepository> {
        AndroidReadoutPreferencesRepository(context.readoutDataStore)
    }
    single<TextToSpeechEngine> { AndroidTtsEngine(context) }
    single<ProximityRepository> { EmptyProximityRepository() }
}

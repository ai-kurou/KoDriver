package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

internal class LmuWindowsPitTimingReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsPitTimingPreferences>,
) : LmuWindowsPitTimingReadoutTextPreferencesRepository {
    override fun observeVirtualEnergyReadoutText(): Flow<String> =
        dataStore.observeProperty { it.virtualEnergyReadoutText }

    override suspend fun saveVirtualEnergyReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(virtualEnergyReadoutText = value) }
    }

    override fun observeVirtualEnergyImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.virtualEnergyImminentReadoutText
        }

    override suspend fun saveVirtualEnergyImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(virtualEnergyImminentReadoutText = value) }
    }

    override fun observeTyreWearReadoutText(): Flow<String> = dataStore.observeProperty { it.tyreWearReadoutText }

    override suspend fun saveTyreWearReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearReadoutText = value) }
    }

    override fun observeTyreWearImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.tyreWearImminentReadoutText
        }

    override suspend fun saveTyreWearImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearImminentReadoutText = value) }
    }
}

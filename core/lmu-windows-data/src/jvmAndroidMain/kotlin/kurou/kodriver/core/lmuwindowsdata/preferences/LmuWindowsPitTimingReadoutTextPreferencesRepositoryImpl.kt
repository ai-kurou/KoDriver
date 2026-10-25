package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

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

    override fun observeFuelReadoutText(): Flow<String> = dataStore.observeProperty { it.fuelReadoutText }

    override suspend fun saveTyreWearReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearReadoutText = value) }
    }

    override suspend fun saveFuelReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(fuelReadoutText = value) }
    }

    override fun observeTyreWearImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.tyreWearImminentReadoutText
        }

    override fun observeFuelImminentReadoutText(): Flow<String> =
        dataStore.observeProperty {
            it.fuelImminentReadoutText
        }

    override suspend fun saveTyreWearImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(tyreWearImminentReadoutText = value) }
    }

    override suspend fun saveFuelImminentReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(fuelImminentReadoutText = value) }
    }
}

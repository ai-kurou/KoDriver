package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

internal class DebugStateCardOrderPreferencesRepositoryImpl(
    private val dataStore: DataStore<DebugStateCardOrderPreferences>,
) : DebugStateCardOrderPreferencesRepository {
    override fun observeCardOrder(): Flow<List<String>> =
        dataStore.observeProperty { prefs ->
            prefs.cardOrder
        }

    override suspend fun saveCardOrder(order: List<String>) {
        dataStore.saveProperty(order) { prefs, value -> prefs.copy(cardOrder = value) }
    }
}

package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.DebugStateCardOrderPreferences
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

internal class DebugStateCardOrderPreferencesRepositoryImpl(
    private val dataStore: DataStore<DebugStateCardOrderPreferences>,
) : DebugStateCardOrderPreferencesRepository {
    override fun observeCardOrder(): Flow<List<DebugStateCardKey>> =
        dataStore.observeProperty { prefs ->
            prefs.cardOrder.mapNotNull { name -> DebugStateCardKey.entries.find { it.name == name } }
        }

    override suspend fun saveCardOrder(order: List<DebugStateCardKey>) {
        dataStore.saveProperty(order) { prefs, value -> prefs.copy(cardOrder = value.map { key -> key.name }) }
    }
}

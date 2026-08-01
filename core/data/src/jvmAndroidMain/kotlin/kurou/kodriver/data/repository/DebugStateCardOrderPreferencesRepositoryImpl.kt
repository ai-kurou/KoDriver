package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.DebugStateCardOrderPreferences
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository

internal class DebugStateCardOrderPreferencesRepositoryImpl(
    private val dataStore: DataStore<DebugStateCardOrderPreferences>,
) : DebugStateCardOrderPreferencesRepository {
    override fun observeCardOrder(): Flow<List<DebugStateCardKey>> =
        dataStore.data.map { prefs ->
            prefs.cardOrder.mapNotNull { name -> DebugStateCardKey.entries.find { it.name == name } }
        }

    override suspend fun saveCardOrder(order: List<DebugStateCardKey>) {
        dataStore.updateData { it.copy(cardOrder = order.map { key -> key.name }) }
    }
}

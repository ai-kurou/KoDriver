package kurou.kodriver.domain.debug.repository

import kotlinx.coroutines.flow.Flow

public interface DebugStateCardOrderPreferencesRepository {
    public fun observeCardOrder(): Flow<List<String>>

    public suspend fun saveCardOrder(order: List<String>)
}

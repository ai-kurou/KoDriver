package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface DebugStateCardOrderPreferencesRepository {
    fun observeCardOrder(): Flow<List<String>>

    suspend fun saveCardOrder(order: List<String>)
}

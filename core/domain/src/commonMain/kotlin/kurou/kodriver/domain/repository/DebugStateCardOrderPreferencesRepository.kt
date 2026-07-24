package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DebugStateCardKey

interface DebugStateCardOrderPreferencesRepository {
    fun observeCardOrder(): Flow<List<DebugStateCardKey>>
    suspend fun saveCardOrder(order: List<DebugStateCardKey>)
}

package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutSet

interface ManualReadoutSetPreferencesRepository {
    fun observeSets(): Flow<List<ManualReadoutSet>>

    suspend fun saveSets(sets: List<ManualReadoutSet>)
}

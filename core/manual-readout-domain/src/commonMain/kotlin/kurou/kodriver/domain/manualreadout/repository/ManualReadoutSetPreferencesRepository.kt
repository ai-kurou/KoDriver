package kurou.kodriver.domain.manualreadout.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutSet

public interface ManualReadoutSetPreferencesRepository {
    public fun observeSets(): Flow<List<ManualReadoutSet>>

    public suspend fun addSet(set: ManualReadoutSet)

    public suspend fun updateSet(set: ManualReadoutSet)

    public suspend fun deleteSet(id: String)

    public suspend fun saveSets(sets: List<ManualReadoutSet>)

    public suspend fun resetToDefault()
}

package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface DynamicColorEnabledRepository {
    fun dynamicColorEnabled(): Flow<Boolean>

    suspend fun saveDynamicColorEnabled(enabled: Boolean)
}

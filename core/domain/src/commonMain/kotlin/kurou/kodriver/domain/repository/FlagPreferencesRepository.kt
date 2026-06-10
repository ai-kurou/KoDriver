package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface FlagPreferencesRepository {
    fun observeFlagEnabledStates(): Flow<Map<String, Boolean>>
    suspend fun saveFlagEnabledState(key: String, enabled: Boolean)
}

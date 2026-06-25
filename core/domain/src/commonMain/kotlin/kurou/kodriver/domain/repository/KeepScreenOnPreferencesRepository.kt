package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeepScreenOnPreferencesRepository {
    fun keepScreenOn(): Flow<Boolean>
    suspend fun saveKeepScreenOn(enabled: Boolean)
}

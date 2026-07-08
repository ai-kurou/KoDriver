package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeepScreenOnEnabledRepository {
    fun keepScreenOn(): Flow<Boolean>
    suspend fun saveKeepScreenOn(enabled: Boolean)
}

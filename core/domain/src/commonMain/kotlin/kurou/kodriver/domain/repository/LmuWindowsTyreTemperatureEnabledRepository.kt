package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsTyreTemperatureEnabledRepository {
    fun observeEnabled(): Flow<Boolean>
    suspend fun saveEnabled(enabled: Boolean)
}

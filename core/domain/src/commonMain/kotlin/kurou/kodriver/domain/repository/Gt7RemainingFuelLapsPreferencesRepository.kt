package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7RemainingFuelLapsPreferencesRepository {
    fun observeEnabled(): Flow<Boolean>
    suspend fun saveEnabled(enabled: Boolean)
}

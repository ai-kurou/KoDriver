package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5RemainingFuelPreferencesRepository {
    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)
}

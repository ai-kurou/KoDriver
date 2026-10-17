package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5RemainingFuelPreferencesRepository {
    fun observeThresholdPercentage(): Flow<Int>

    suspend fun saveThresholdPercentage(percentage: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)
}

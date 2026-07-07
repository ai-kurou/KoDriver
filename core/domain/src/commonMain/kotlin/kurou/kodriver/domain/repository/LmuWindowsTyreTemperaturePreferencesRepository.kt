package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Int>
    suspend fun saveHighThresholdCelsius(celsius: Int)
}

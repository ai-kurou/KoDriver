package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface LmuWindowsBrakeTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Int>

    suspend fun saveHighThresholdCelsius(celsius: Int)
}

package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5TyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Int>

    suspend fun saveHighThresholdCelsius(celsius: Int)
}

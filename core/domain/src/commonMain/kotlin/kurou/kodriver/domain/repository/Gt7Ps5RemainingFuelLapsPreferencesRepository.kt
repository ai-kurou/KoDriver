package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5RemainingFuelLapsPreferencesRepository {
    fun observeRemainingFuelLaps(): Flow<Int>
    suspend fun saveRemainingFuelLaps(laps: Int)
}

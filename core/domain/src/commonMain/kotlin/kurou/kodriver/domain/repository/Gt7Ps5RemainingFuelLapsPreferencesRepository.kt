package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7Ps5RemainingFuelLapsPreferencesRepository {
    fun observeRemainingFuelLaps(): Flow<Int>

    suspend fun saveRemainingFuelLaps(laps: Int)

    fun observeReadoutText(): Flow<String>

    suspend fun saveReadoutText(text: String)

    fun observeEmptyReadoutText(): Flow<String>

    suspend fun saveEmptyReadoutText(text: String)
}

package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow

public interface Gt7Ps5RemainingFuelLapsPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeRemainingFuelLaps(): Flow<Int>

    public suspend fun saveRemainingFuelLaps(laps: Int)

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)

    public fun observeEmptyReadoutText(): Flow<String>

    public suspend fun saveEmptyReadoutText(text: String)
}

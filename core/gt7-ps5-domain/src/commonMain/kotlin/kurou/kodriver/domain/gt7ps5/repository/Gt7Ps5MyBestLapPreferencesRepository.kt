package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow

public interface Gt7Ps5MyBestLapPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeReadoutText(): Flow<String>

    public suspend fun saveReadoutText(text: String)
}

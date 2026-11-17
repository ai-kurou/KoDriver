package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface KeepScreenOnEnabledRepository {
    public fun keepScreenOn(): Flow<Boolean>

    public suspend fun saveKeepScreenOn(enabled: Boolean)
}

package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface DynamicColorEnabledRepository {
    public fun dynamicColorEnabled(): Flow<Boolean>

    public suspend fun saveDynamicColorEnabled(enabled: Boolean)
}

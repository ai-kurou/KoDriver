package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface HapticFeedbackEnabledRepository {
    public fun hapticFeedbackEnabled(): Flow<Boolean>

    public suspend fun saveHapticFeedbackEnabled(enabled: Boolean)
}

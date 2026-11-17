package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface FeedbackCooldownPreferencesRepository {
    public fun lastFeedbackSentAtEpochMillis(): Flow<Long?>

    public suspend fun saveLastFeedbackSentAtEpochMillis(epochMillis: Long)
}

package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface FeedbackCooldownPreferencesRepository {
    fun lastFeedbackSentAtEpochMillis(): Flow<Long?>

    suspend fun saveLastFeedbackSentAtEpochMillis(epochMillis: Long)
}

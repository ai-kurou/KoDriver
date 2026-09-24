package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository

internal class FeedbackCooldownPreferencesRepositoryImpl(
    private val dataStore: DataStore<FeedbackCooldownPreferences>,
) : FeedbackCooldownPreferencesRepository {
    override fun lastFeedbackSentAtEpochMillis(): Flow<Long?> =
        dataStore.observeProperty { it.lastSentAtEpochMillis.takeIf { millis -> millis > 0L } }

    override suspend fun saveLastFeedbackSentAtEpochMillis(epochMillis: Long) {
        dataStore.saveProperty(epochMillis) { prefs, value -> prefs.copy(lastSentAtEpochMillis = value) }
    }
}

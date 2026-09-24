package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createFeedbackCooldownDataStore(directory: String): DataStore<FeedbackCooldownPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "feedback_cooldown.pb",
        serializer = FeedbackCooldownSerializer,
    )

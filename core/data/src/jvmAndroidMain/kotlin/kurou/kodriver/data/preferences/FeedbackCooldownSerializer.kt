package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val FeedbackCooldownSerializer: Serializer<FeedbackCooldownPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = FeedbackCooldownPreferences(),
        kSerializer = FeedbackCooldownPreferences.serializer(),
    )

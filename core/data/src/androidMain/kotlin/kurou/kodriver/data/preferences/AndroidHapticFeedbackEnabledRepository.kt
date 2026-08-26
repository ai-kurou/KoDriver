package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.HAPTIC_FEEDBACK_ENABLED_DEFAULT
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository

internal class AndroidHapticFeedbackEnabledRepository(
    private val dataStore: DataStore<Preferences>,
) : HapticFeedbackEnabledRepository {
    private val hapticFeedbackEnabledKey = booleanPreferencesKey("haptic_feedback_enabled")

    override fun hapticFeedbackEnabled(): Flow<Boolean> =
        dataStore.data.map {
            it[hapticFeedbackEnabledKey]
                ?: HAPTIC_FEEDBACK_ENABLED_DEFAULT
        }

    override suspend fun saveHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { it[hapticFeedbackEnabledKey] = enabled }
    }
}

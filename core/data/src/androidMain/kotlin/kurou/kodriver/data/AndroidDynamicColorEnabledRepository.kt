package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.DYNAMIC_COLOR_ENABLED_DEFAULT
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

internal class AndroidDynamicColorEnabledRepository(
    private val dataStore: DataStore<Preferences>,
) : DynamicColorEnabledRepository {
    private val dynamicColorEnabledKey = booleanPreferencesKey("dynamic_color_enabled")

    override fun dynamicColorEnabled(): Flow<Boolean> =
        dataStore.data.map { it[dynamicColorEnabledKey] ?: DYNAMIC_COLOR_ENABLED_DEFAULT }

    override suspend fun saveDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[dynamicColorEnabledKey] = enabled }
    }
}

package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.KEEP_SCREEN_ON_ENABLED_DEFAULT
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

internal class AndroidKeepScreenOnEnabledRepository(
    private val dataStore: DataStore<Preferences>,
) : KeepScreenOnEnabledRepository {
    private val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")

    override fun keepScreenOn(): Flow<Boolean> = dataStore.data.map { it[keepScreenOnKey] ?: KEEP_SCREEN_ON_ENABLED_DEFAULT }

    override suspend fun saveKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[keepScreenOnKey] = enabled }
    }
}

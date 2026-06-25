package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository

internal class AndroidKeepScreenOnPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : KeepScreenOnPreferencesRepository {

    private val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")

    override fun keepScreenOn(): Flow<Boolean> = dataStore.data.map { it[keepScreenOnKey] ?: true }

    override suspend fun saveKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[keepScreenOnKey] = enabled }
    }
}

package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository

internal class AndroidExitConfirmationPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : ExitConfirmationPreferencesRepository {

    private val exitConfirmationEnabledKey = booleanPreferencesKey("exit_confirmation_enabled")

    override fun exitConfirmationEnabled(): Flow<Boolean> =
        dataStore.data.map { it[exitConfirmationEnabledKey] ?: true }

    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) {
        dataStore.edit { it[exitConfirmationEnabledKey] = enabled }
    }
}

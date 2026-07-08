package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import java.io.File

internal class JvmExitConfirmationEnabledRepository(
    private val dataStore: DataStore<Preferences>,
) : ExitConfirmationEnabledRepository {

    private val exitConfirmationEnabledKey = booleanPreferencesKey("exit_confirmation_enabled")

    override fun exitConfirmationEnabled(): Flow<Boolean> =
        dataStore.data.map { it[exitConfirmationEnabledKey] ?: true }.catch { emit(true) }

    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) {
        dataStore.edit { it[exitConfirmationEnabledKey] = enabled }
    }
}

internal fun createExitConfirmationPreferencesDataStore(directory: String): DataStore<Preferences> {
    val dir = File(directory).also { it.mkdirs() }
    return androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
        produceFile = { dir.resolve("exit_confirmation_preferences.preferences_pb") },
    )
}

fun createExitConfirmationEnabledRepository(directory: String): ExitConfirmationEnabledRepository =
    JvmExitConfirmationEnabledRepository(createExitConfirmationPreferencesDataStore(directory))

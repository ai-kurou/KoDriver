package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.EXIT_CONFIRMATION_ENABLED_DEFAULT
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import java.io.File
import java.io.IOException

internal class JvmExitConfirmationEnabledRepository(
    private val dataStore: DataStore<Preferences>,
) : ExitConfirmationEnabledRepository {
    private val exitConfirmationEnabledKey = booleanPreferencesKey("exit_confirmation_enabled")

    override fun exitConfirmationEnabled(): Flow<Boolean> =
        dataStore.data
            .map { it[exitConfirmationEnabledKey] ?: EXIT_CONFIRMATION_ENABLED_DEFAULT }
            .catch {
                Sentry.captureException(it)
                emit(EXIT_CONFIRMATION_ENABLED_DEFAULT)
            }

    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) {
        try {
            dataStore.edit { it[exitConfirmationEnabledKey] = enabled }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            // Windows はファイルロックの挙動が macOS/Linux と異なり、DataStore の
            // atomic write（一時ファイルへの書き込み後リネーム）がアンチウイルス等の
            // 一時的なロックで失敗しやすい。保存失敗時に例外を投げると呼び出し元の
            // viewModelScope で未処理のまま握りつぶされ、原因調査ができなくなるため
            // Sentry へ送信したうえで処理を継続する。
            Sentry.captureException(e)
        }
    }
}

internal fun createExitConfirmationPreferencesDataStore(directory: String): DataStore<Preferences> {
    val dir = File(directory).also { it.mkdirs() }
    return androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
        produceFile = { dir.resolve("exit_confirmation_preferences.preferences_pb") },
    )
}

/**
 * ExitConfirmationEnabled Repository の永続化実装を生成する。
 */
fun createExitConfirmationEnabledRepository(directory: String): ExitConfirmationEnabledRepository =
    JvmExitConfirmationEnabledRepository(createExitConfirmationPreferencesDataStore(directory))

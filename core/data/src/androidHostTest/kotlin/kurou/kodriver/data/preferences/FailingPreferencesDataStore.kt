package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * 読み取りが必ず [IOException] で失敗する `DataStore<Preferences>`。
 *
 * `Android*Repository` のフォールバック挙動を確認するためのテスト用フェイク。
 */
internal class FailingPreferencesDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw IOException("read failed") }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        throw UnsupportedOperationException("書き込みはテスト対象外")
}

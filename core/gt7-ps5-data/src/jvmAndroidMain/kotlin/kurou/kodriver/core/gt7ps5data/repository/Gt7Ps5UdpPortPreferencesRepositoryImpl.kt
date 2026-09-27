package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStore
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import java.io.IOException

internal class Gt7Ps5UdpPortPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5UdpPortPreferences>,
) : Gt7Ps5UdpPortPreferencesRepository {
    /**
     * 読み取りに失敗（[IOException]）した場合はデフォルトポートを1回 emit して正常終了する。
     * 値が1つも届かないと接続先ポートが決まらず UI が設定待ちのまま固まるため、
     * 失敗そのものは [Sentry] に記録したうえでデフォルト値で動作を継続する。
     */
    override fun port(): Flow<Int> =
        dataStore.data
            .map { it.port }
            .catch { e ->
                if (e is IOException) {
                    Sentry.captureException(e)
                    emit(Gt7Ps5UdpPortPreferences().port)
                } else {
                    throw e
                }
            }

    override suspend fun savePort(port: Int) {
        dataStore.updateData { it.copy(port = port) }
    }
}

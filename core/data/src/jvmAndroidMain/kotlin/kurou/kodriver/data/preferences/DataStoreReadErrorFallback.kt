package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * `DataStore.data` の読み取り失敗（[IOException]）時に [defaultValue] を1回だけ emit して正常終了させる。
 *
 * 読み取りに失敗した Flow をそのまま流すと購読側へ値が1つも届かず、UI が設定待ちのまま固まる。
 * 破損（`SerializationException`）を Sentry へ送る `protoBufPreferencesSerializer` の方針に揃え、
 * 失敗そのものは [Sentry] に記録したうえでデフォルト値で動作を継続する。
 * [IOException] 以外の例外はこれまでどおり伝播させる。
 */
internal fun <T> Flow<T>.fallbackOnReadError(defaultValue: T): Flow<T> =
    catch { e ->
        if (e is IOException) {
            Sentry.captureException(e)
            emit(defaultValue)
        } else {
            throw e
        }
    }

/**
 * [DataStore.data] にのみ [fallbackOnReadError] を適用する薄いデコレータ。
 *
 * 書き込み（[DataStore.updateData]）の挙動は変更しない。
 */
internal class FallbackOnReadErrorDataStore<T>(
    private val delegate: DataStore<T>,
    defaultValue: T,
) : DataStore<T> {
    override val data: Flow<T> = delegate.data.fallbackOnReadError(defaultValue)

    override suspend fun updateData(transform: suspend (t: T) -> T): T = delegate.updateData(transform)
}

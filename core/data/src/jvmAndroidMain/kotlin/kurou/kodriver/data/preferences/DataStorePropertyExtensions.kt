package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [DataStore] の値からプロパティ単位で購読する [RepositoryImpl] 共通の薄いラッパー。
 *
 * 各 Preferences の Repository インターフェースはプロパティ単位のメソッド
 * （`observeXxx()` / `saveXxx()`）を持つため、DataStore 全体ではなく
 * プロパティの取得・更新だけを差分として渡す形に共通化する。
 */
internal fun <T, V> DataStore<T>.observeProperty(get: (T) -> V): Flow<V> = data.map(get)

internal suspend fun <T, V> DataStore<T>.saveProperty(
    value: V,
    update: (preferences: T, value: V) -> T,
) {
    updateData { update(it, value) }
}

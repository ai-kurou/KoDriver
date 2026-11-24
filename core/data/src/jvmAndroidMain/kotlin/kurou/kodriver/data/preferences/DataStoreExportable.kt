package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kurou.kodriver.domain.exportimport.Exportable

/**
 * [DataStore] が保持する Preferences を JSON としてエクスポート・インポートする共通実装。
 *
 * Repository 実装がこのクラスへ委譲して [Exportable] を満たす。DataStore は同じファイルに対して
 * 複数開けないため、Repository が抱える DataStore をそのまま使う。インポートは [DataStore.updateData]
 * で書き戻すので、実行中の購読にも反映される。
 *
 * @param key エクスポート先 JSON のキー。ASCII の内部 ID にし、一度公開したら変更しない。
 * @param sanitize インポートした値を保存前に補正する。範囲外の値を許容範囲へ収めるために使う。
 */
internal class DataStoreExportable<T>(
    override val key: String,
    private val dataStore: DataStore<T>,
    private val serializer: KSerializer<T>,
    private val sanitize: (T) -> T = { it },
) : Exportable {
    override suspend fun export(): JsonElement = json.encodeToJsonElement(serializer, dataStore.data.first())

    override suspend fun import(value: JsonElement) {
        val imported = sanitize(json.decodeFromJsonElement(serializer, value))
        dataStore.updateData { imported }
    }

    private companion object {
        // 既定値のプロパティも書き出し、将来の既定値変更の影響を受けないようにする。
        val json =
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
    }
}

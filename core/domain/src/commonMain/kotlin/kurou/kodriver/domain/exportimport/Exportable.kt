package kurou.kodriver.domain.exportimport

import kotlinx.serialization.json.JsonElement

/**
 * 設定値のエクスポート/インポート対象 1 件（1 つの DataStore または Repository）を表す。
 *
 * 新しい設定を追加したときは、対応する [Exportable] を DI で登録すること。
 * 端末固有の値（マイクのデバイス ID・モデルのパス・ポートなど）は対象に含めない。
 */
public interface Exportable {
    /** エクスポート JSON の `settings` で使う一意なキー。一度公開したら変更しない。 */
    public val key: String

    /** 現在値を JSON にして返す。 */
    public suspend fun export(): JsonElement

    /** [value] を検証して保存する。不正な値の場合は例外を投げる。 */
    public suspend fun import(value: JsonElement)
}

package kurou.kodriver.domain.model

/**
 * 読み上げ機能を識別する永続化キー。
 *
 * [value] は DataStore の設定キー、読み上げ一覧の並び順、キュー可否判定で共有する安定値。
 * 既存値を変更すると保存済み設定が失われるため、表示文言の変更とは独立して扱う。
 * 自動読み上げの保存値にはシミュレーター接頭辞の直後に `auto_` を置く。
 * `auto_` 導入時はユーザー了承のもと設定を意図的に初期化し、旧値の移行は行わない。
 *
 * `Root` は一覧画面のトップレベル項目、その他のキーは詳細画面内のサブ項目を表す。
 */
interface ReadoutItemKey {
    /** DataStore に保存する安定識別子。 */
    val value: String

    /**
     * listPane のトップレベル項目（Root）であることを表すマーカー。
     * キューへ積んで後で読み上げてよいかどうかは Root 単位でのみ判定するため、
     * supportsQueue は TopLevel にのみ存在し、サブ項目には存在しない。
     * デフォルト値は持たせず、新規追加時に必ず true/false を明示させる。
     */
    interface TopLevel : ReadoutItemKey {
        val supportsQueue: Boolean
    }

    companion object {
        /** 既存保存値の復元対象となる全キー。各 simulator モジュールの登録済みキーを返す。 */
        val entries: List<ReadoutItemKey> get() = ReadoutItemKeyRegistry.entries

        /** シミュレーター固有の一覧項目を、entries の順序で返す。 */
        fun topLevelEntriesOf(simulator: Simulator): List<TopLevel> =
            ReadoutItemKeyRegistry.topLevelEntriesOf(simulator)

        /** DataStore に保存された [value] からキーを復元する。不明な値は null を返す。 */
        fun fromValue(value: String): ReadoutItemKey? = ReadoutItemKeyRegistry.fromValue(value)
    }
}

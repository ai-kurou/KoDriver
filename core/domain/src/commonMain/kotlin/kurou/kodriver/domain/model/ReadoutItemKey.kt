package kurou.kodriver.domain.model

/**
 * 読み上げ機能を識別する永続化キー。
 *
 * [value] は DataStore の設定キー、読み上げ一覧の並び順、キュー可否判定で共有する安定値。
 * 既存値を変更すると保存済み設定が失われるため、表示文言の変更とは独立して扱う。
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
        /**
         * 既存保存値の復元対象となる全キーを、LMU・GT7・ACEの順で集約する。
         * シミュレーター間の集約はここ1か所に閉じ、将来はこの集約部分だけを集約側モジュールへ移せる。
         * 各シミュレーター内のキーと並び順は、それぞれの entries で管理する。
         */
        val entries: List<ReadoutItemKey> by lazy {
            LmuWindowsReadoutItemKey.entries + Gt7Ps5ReadoutItemKey.entries + AceWindowsReadoutItemKey.entries
        }

        /** DataStore に保存された [value] からキーを復元する。不明な値は null を返す。 */
        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}

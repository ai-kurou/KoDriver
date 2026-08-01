package kurou.kodriver.domain.model

/**
 * KoDriver が扱う走行データ取得元。
 *
 * [id] は DataStore・WebSocket URL・読み上げ設定の永続化キーで共有する安定識別子。
 * 表示名ではないため、既存値を変更すると保存済み設定や外部連携 URL と互換性がなくなる。
 *
 * [requiresKoDriverServer] が true のシミュレータは、Android 側から直接データを読めないため
 * Windows デスクトップアプリ内の KoDriver サーバー経由で購読する。
 */
sealed class Simulator(
    val id: String,
    val requiresKoDriverServer: Boolean,
) {
    /** LMU の Windows 共有メモリを、Windows デスクトップアプリ経由で読むシミュレータ。 */
    data object LmuWindows : Simulator(id = "lmu_windows", requiresKoDriverServer = true)

    /** GT7 の PS5 UDP テレメトリを、Android / Desktop から直接読むシミュレータ。 */
    data object Gt7Ps5 : Simulator(id = "gt7_ps5", requiresKoDriverServer = false)

    /** Assetto Corsa EVO の Windows 共有メモリを、Windows デスクトップアプリ経由で読むシミュレータ。 */
    data object AceWindows : Simulator(id = "ace_windows", requiresKoDriverServer = true)

    companion object {
        private val entries by lazy { listOf(LmuWindows, Gt7Ps5, AceWindows) }

        // nullは「まだシミュレーターが選択されていない」という正当な初期状態を表すため、非null化してデフォルト値にフォールバックしてはならない。

        /** 永続化された [id] からシミュレータを復元する。不明な値や未選択状態は null として扱う。 */
        fun fromId(id: String): Simulator? = entries.find { it.id == id }
    }
}

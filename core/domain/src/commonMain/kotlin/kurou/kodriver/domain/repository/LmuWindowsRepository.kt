package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsTelemetryData

/**
 * LMU の Windows 共有メモリへアクセスする Repository。
 *
 * Windows 専用の実装を想定する。未対応 platform や未接続状態では
 * [isConnected] が false を返し、[telemetryStream] は有効なテレメトリを流さない。
 */
interface LmuWindowsRepository {
    /** プレイヤー車両のテレメトリを接続中に継続配信する cold Flow。 */
    fun telemetryStream(): Flow<LmuWindowsTelemetryData>

    /** 現在 shared memory へ接続できているかを単発で確認する。 */
    suspend fun isConnected(): Boolean

    /** shared memory reader を閉じ、次回購読時に再接続できる状態に戻す。 */
    suspend fun disconnect()
}

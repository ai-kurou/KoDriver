package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.Gt7Ps5TelemetryData

/**
 * GT7 の PS5 UDP テレメトリを受信する Repository。
 *
 * 接続状態は UDP パケットの受信状況で判断する。PS5 と同一 LAN 上にない場合や
 * コンソールアドレスが未設定の場合は [isConnected] が false になる。
 */
interface Gt7Ps5Repository {
    /** 受信した GT7 UDP パケットをアプリ内テレメトリへ変換して流す cold Flow。 */
    fun telemetryStream(): Flow<Gt7Ps5TelemetryData>

    /** 現在 UDP テレメトリを受信できているかを単発で確認する。 */
    suspend fun isConnected(): Boolean
}

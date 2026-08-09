package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.AceWindowsFuelData

/**
 * Assetto Corsa EVO の Windows 共有メモリから燃料情報を読む Repository。
 */
interface AceWindowsFuelRepository {
    /** 接続中の燃料残量を継続配信する cold Flow。 */
    fun fuelStream(): Flow<AceWindowsFuelData>

    /** 現在 shared memory へ接続できているかを単発で確認する。 */
    suspend fun isConnected(): Boolean
}

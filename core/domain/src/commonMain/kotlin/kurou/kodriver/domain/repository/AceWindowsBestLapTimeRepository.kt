package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData

/**
 * Assetto Corsa EVO の Windows 共有メモリからベストラップタイムを読む Repository。
 */
interface AceWindowsBestLapTimeRepository {
    /** セッション中のベストラップタイムを継続配信する cold Flow。 */
    fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData>
}

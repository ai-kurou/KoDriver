package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData

/**
 * Assetto Corsa EVO の Windows 共有メモリからベストラップタイムを読む Repository。
 */
public interface AceWindowsBestLapTimeRepository {
    /** セッション中のベストラップタイムを継続配信する cold Flow。 */
    public fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData>
}

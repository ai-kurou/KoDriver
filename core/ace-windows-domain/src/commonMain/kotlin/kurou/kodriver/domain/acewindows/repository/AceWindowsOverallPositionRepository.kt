package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData

/**
 * Assetto Corsa EVO の Windows 共有メモリから総合順位・総台数を読む Repository。
 */
interface AceWindowsOverallPositionRepository {
    /** 総合順位・総台数を継続配信する cold Flow。 */
    fun overallPositionStream(): Flow<AceWindowsOverallPositionData>
}

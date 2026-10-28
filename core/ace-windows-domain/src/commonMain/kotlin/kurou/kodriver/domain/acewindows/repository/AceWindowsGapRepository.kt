package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData

/**
 * Assetto Corsa EVO の Windows 共有メモリから前車・後車とのギャップを読む Repository。
 */
interface AceWindowsGapRepository {
    /** 前車・後車とのギャップを継続配信する cold Flow。 */
    fun gapStream(): Flow<AceWindowsGapData>
}

package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData

/**
 * Assetto Corsa EVO の Windows 共有メモリから残燃料で走行可能な周回数を読む Repository。
 */
interface AceWindowsRemainingFuelLapsRepository {
    /** 残燃料で走行可能な周回数を継続配信する cold Flow。 */
    fun remainingFuelLapsStream(): Flow<AceWindowsRemainingFuelLapsData>
}

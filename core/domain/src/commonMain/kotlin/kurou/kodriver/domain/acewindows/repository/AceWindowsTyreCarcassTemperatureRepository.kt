package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData

/**
 * Assetto Corsa EVO の Windows 共有メモリからタイヤのカーカス平均温度を読む Repository。
 */
interface AceWindowsTyreCarcassTemperatureRepository {
    /** 4輪分のカーカス平均温度を継続配信する cold Flow。 */
    fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData>
}

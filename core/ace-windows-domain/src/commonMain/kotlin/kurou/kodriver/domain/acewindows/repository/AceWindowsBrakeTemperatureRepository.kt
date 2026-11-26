package kurou.kodriver.domain.acewindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsBrakeTemperatureData

/**
 * Assetto Corsa EVO の Windows 共有メモリからブレーキ温度を読む Repository。
 */
public interface AceWindowsBrakeTemperatureRepository {
    /** 4輪分のブレーキ温度を継続配信する cold Flow。 */
    public fun brakeTemperatureStream(): Flow<AceWindowsBrakeTemperatureData>
}

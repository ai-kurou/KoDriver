package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData

/** デスクトップ版は LMU REST API、Android 版は KoDriver サーバーの WebSocket から取得する。 */
public interface LmuWindowsBrakeWearRepository {
    /** ブレーキ残り厚さ。取得に失敗した（LMU 未起動など）周期は null を流す。 */
    public fun brakeWearStream(): Flow<LmuWindowsBrakeWearData?>
}

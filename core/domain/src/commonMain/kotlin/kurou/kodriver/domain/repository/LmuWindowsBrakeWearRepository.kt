package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData

/** デスクトップ版は LMU REST API、Android 版は KoDriver サーバーの WebSocket から取得する。 */
interface LmuWindowsBrakeWearRepository {
    /** ブレーキ残り厚さ。取得に失敗した（LMU 未起動など）周期は null を流す。 */
    fun brakeWearStream(): Flow<LmuWindowsBrakeWearData?>
}

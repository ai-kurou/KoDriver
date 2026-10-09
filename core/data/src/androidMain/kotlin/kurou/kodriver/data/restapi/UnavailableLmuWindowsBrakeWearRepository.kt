package kurou.kodriver.data.restapi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository

/**
 * LMU 内蔵 REST API は LMU を起動した Windows 機の `localhost` にしか待ち受けないため、
 * Android からは取得できない。何も流さない。
 */
internal class UnavailableLmuWindowsBrakeWearRepository : LmuWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<LmuWindowsBrakeWearData> = emptyFlow()
}

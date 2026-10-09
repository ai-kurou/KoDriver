package kurou.kodriver.data.restapi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository

/**
 * LMU 内蔵 REST API は LMU を起動した Windows 機の `localhost` にしか待ち受けないため、
 * Android からは取得できない。常に「取得できなかった」状態の 1 件だけを流す。
 */
internal class UnavailableLmuWindowsBrakeWearInvestigationRepository : LmuWindowsBrakeWearInvestigationRepository {
    override fun investigationStream(): Flow<LmuWindowsBrakeWearInvestigationData> =
        flowOf(LmuWindowsBrakeWearInvestigationData())
}

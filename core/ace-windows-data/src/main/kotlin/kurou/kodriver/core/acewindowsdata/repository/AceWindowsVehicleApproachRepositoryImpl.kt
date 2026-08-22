package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository

internal class AceWindowsVehicleApproachRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData> =
        source.bufferFlow.map { AceWindowsMapper.mapVehicleApproach(it) }
}

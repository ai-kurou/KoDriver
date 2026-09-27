package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository

internal class AceWindowsRemainingFuelLapsRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsRemainingFuelLapsRepository {
    override fun remainingFuelLapsStream(): Flow<AceWindowsRemainingFuelLapsData> =
        source.bufferFlow.map { AceWindowsMapper.mapRemainingFuelLaps(it) }
}

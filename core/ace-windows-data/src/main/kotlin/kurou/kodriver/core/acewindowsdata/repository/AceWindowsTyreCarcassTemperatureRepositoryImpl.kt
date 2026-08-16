package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository

internal class AceWindowsTyreCarcassTemperatureRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> =
        source.bufferFlow.map { AceWindowsMapper.mapTyreCarcassTemperature(it) }
}

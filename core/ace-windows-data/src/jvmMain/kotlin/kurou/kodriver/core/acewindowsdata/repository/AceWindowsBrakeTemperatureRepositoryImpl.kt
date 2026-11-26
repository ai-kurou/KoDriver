package kurou.kodriver.core.acewindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.acewindowsdata.datasource.AceWindowsGraphicsSharedMemorySource
import kurou.kodriver.core.acewindowsdata.mapper.AceWindowsMapper
import kurou.kodriver.domain.acewindows.model.AceWindowsBrakeTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperatureRepository

internal class AceWindowsBrakeTemperatureRepositoryImpl(
    private val source: AceWindowsGraphicsSharedMemorySource,
) : AceWindowsBrakeTemperatureRepository {
    override fun brakeTemperatureStream(): Flow<AceWindowsBrakeTemperatureData> =
        source.bufferFlow.map { AceWindowsMapper.mapBrakeTemperature(it) }
}

package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository
import java.nio.ByteBuffer

internal class LmuWindowsTyreCarcassTemperatureRepository(
    private val source: LmuWindowsSharedMemorySource,
) : TyreCarcassTemperatureRepository {

    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> =
        source.bufferFlow.mapNotNull { readTyreCarcassTemperature(it) }

    private fun readTyreCarcassTemperature(buffer: ByteBuffer): TyreCarcassTemperatureData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val wheels = LmuWindowsMapper.readCarcassTemperaturesK(buffer, vehicleBase)
            .mapValues { (_, kelvin) -> kelvin - KELVIN_OFFSET }
        return TyreCarcassTemperatureData(wheels)
    }

    private companion object {
        const val KELVIN_OFFSET = 273.15
    }
}

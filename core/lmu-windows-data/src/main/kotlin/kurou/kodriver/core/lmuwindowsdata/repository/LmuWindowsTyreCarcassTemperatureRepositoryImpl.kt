package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import java.nio.ByteBuffer

internal class LmuWindowsTyreCarcassTemperatureRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> =
        source.bufferFlow.mapNotNull { readTyreCarcassTemperature(it) }

    private fun readTyreCarcassTemperature(buffer: ByteBuffer): LmuWindowsTyreCarcassTemperatureData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val wheels =
            LmuWindowsMapper
                .readCarcassTemperaturesK(buffer, vehicleBase)
                .mapValues { (_, kelvin) -> CelsiusReading((kelvin - KELVIN_OFFSET).toFloat()) }
        return LmuWindowsTyreCarcassTemperatureData(wheels)
    }

    private companion object {
        const val KELVIN_OFFSET = 273.15
    }
}

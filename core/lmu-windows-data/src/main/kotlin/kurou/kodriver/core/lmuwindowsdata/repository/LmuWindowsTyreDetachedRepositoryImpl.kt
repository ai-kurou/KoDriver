package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsTyreDetachedRepository
import java.nio.ByteBuffer

internal class LmuWindowsTyreDetachedRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsTyreDetachedRepository {
    override fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData> =
        source.bufferFlow.mapNotNull { readTyreDetached(it) }

    private fun readTyreDetached(buffer: ByteBuffer): LmuWindowsTyreDetachedData? {
        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buffer) ?: return null
        val wheels =
            WheelIndex.entries.associateWith { wheel ->
                val wheelBase = vehicleBase + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
                buffer.get(wheelBase + OFF_WHEEL_DETACHED).toInt() != 0
            }
        return LmuWindowsTyreDetachedData(wheels)
    }

    companion object {
        private const val OFF_WHEELS = 848
        private const val WHEEL_STRIDE = 260
        private const val OFF_WHEEL_DETACHED = 178
    }
}

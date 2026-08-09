package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import java.nio.ByteBuffer

internal class LmuWindowsVehicleClassRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsVehicleClassRepository {
    // プレイヤー車両が Scoring から見つからない間も空文字列を emit し続ける（mapNotNull で
    // フィルタすると、Narrator や DebugStateDetailViewModel の combine が他の必須ソースと
    // 合わせて一度も発火しなくなるため）。
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = source.bufferFlow.map { readVehicleClass(it) }

    private fun readVehicleClass(buffer: ByteBuffer): LmuWindowsVehicleClassData {
        val vehicleScoringBase =
            LmuWindowsMapper.findPlayerVehicleScoringBase(buffer)
                ?: return LmuWindowsVehicleClassData.fromRawValue(raw = "")
        val name = LmuWindowsMapper.readVehicleClassName(buffer, vehicleScoringBase)
        return LmuWindowsVehicleClassData.fromRawValue(name)
    }
}

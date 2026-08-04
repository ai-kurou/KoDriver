package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.mapper.LmuWindowsMapper
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import java.nio.ByteBuffer

internal class LmuWindowsPitStatusRepositoryImpl(
    private val source: LmuWindowsSharedMemorySource,
) : LmuWindowsPitStatusRepository {
    // プレイヤー車両が Scoring から見つからない間も既定値を emit し続ける（LmuWindowsVehicleClassRepositoryImpl と同様、
    // mapNotNull でフィルタすると他の必須ソースと合わせた combine が一度も発火しなくなるため）。
    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = source.bufferFlow.map { readPitStatus(it) }

    private fun readPitStatus(buffer: ByteBuffer): LmuWindowsPitStatusData {
        val vehicleScoringBase =
            LmuWindowsMapper.findPlayerVehicleScoringBase(buffer)
                ?: return LmuWindowsPitStatusData(
                    inPits = false,
                    pitState = LmuWindowsPitState.UNKNOWN,
                    inGarageStall = false,
                )
        return LmuWindowsMapper.readPitStatus(buffer, vehicleScoringBase)
    }
}

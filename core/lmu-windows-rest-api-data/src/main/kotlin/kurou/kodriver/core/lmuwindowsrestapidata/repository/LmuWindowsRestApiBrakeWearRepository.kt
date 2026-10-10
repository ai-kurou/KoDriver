package kurou.kodriver.core.lmuwindowsrestapidata.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiRepairAndRefuelDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.mapper.LmuWindowsRestApiBrakeWearMapper
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository

/**
 * `wearables.brakes`（ブレーキ残り厚さ）を一定間隔でポーリングして流す。
 *
 * 取得に失敗（LMU 未起動・HTTP エラー・JSON 不正など）した回、または 4 輪分が揃っていない回は null を流し、
 * 次の周期で再取得する。
 */
internal class LmuWindowsRestApiBrakeWearRepository(
    private val dataSource: LmuWindowsRestApiRepairAndRefuelDataSource,
    private val pollIntervalMillis: Long = POLL_INTERVAL_MILLIS,
) : LmuWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<LmuWindowsBrakeWearData?> =
        flow {
            while (true) {
                emit(fetchOrNull())
                delay(pollIntervalMillis)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchOrNull(): LmuWindowsBrakeWearData? =
        try {
            LmuWindowsRestApiBrakeWearMapper.toBrakeWear(dataSource.fetchRepairAndRefuel())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1_000L
    }
}

package kurou.kodriver.core.lmuwindowsrestapidata.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiBrakeInfoDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiRepairAndRefuelDataSource
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository

/**
 * ブレーキ摩耗の実機調査用に、`wearables.brakes` と `brakeinfo` を一定間隔でポーリングして生の値を流す。
 *
 * どちらか一方の取得に失敗（LMU 未起動・HTTP エラー・JSON 不正など）しても、もう一方は流し続ける。
 * 失敗した側は null になる。
 */
internal class LmuWindowsRestApiBrakeWearInvestigationRepository(
    private val repairAndRefuelDataSource: LmuWindowsRestApiRepairAndRefuelDataSource,
    private val brakeInfoDataSource: LmuWindowsRestApiBrakeInfoDataSource,
    private val pollIntervalMillis: Long = POLL_INTERVAL_MILLIS,
) : LmuWindowsBrakeWearInvestigationRepository {
    override fun investigationStream(): Flow<LmuWindowsBrakeWearInvestigationData> =
        flow {
            while (true) {
                emit(
                    LmuWindowsBrakeWearInvestigationData(
                        wearablesBrakes =
                            fetchOrNull { repairAndRefuelDataSource.fetchRepairAndRefuel().wearables?.brakes },
                        brakeInfo = fetchOrNull { brakeInfoDataSource.fetchBrakeInfo() },
                    ),
                )
                delay(pollIntervalMillis)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> fetchOrNull(block: suspend () -> T?): T? =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1_000L
    }
}

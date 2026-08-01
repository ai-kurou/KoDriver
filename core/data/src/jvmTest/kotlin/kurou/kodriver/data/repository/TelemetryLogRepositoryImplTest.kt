package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.TelemetryLogDao
import kurou.kodriver.data.model.TelemetryLogEntity
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TelemetryLogRepositoryImplTest {
    @Test
    fun `saveTelemetryLogはEntityへ変換して保存する`() =
        runTest {
        val dao = FakeTelemetryLogDao()
        val repository = TelemetryLogRepositoryImpl(dao)

        repository.saveTelemetryLog(
            createdAt = 1000L,
            simulator = Simulator.Gt7Ps5,
            readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetryJson = """{"lapCount":1}""",
        )

        assertEquals(
            listOf(
                TelemetryLogEntity(
                    createdAt = 1000L,
                    simulatorId = Simulator.Gt7Ps5.id,
                    readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
                    telemetryJson = """{"lapCount":1}""",
                ),
            ),
            dao.logs.first(),
        )
    }

    @Test
    fun `deleteAllTelemetryLogsは全てのログを削除する`() =
        runTest {
        val dao =
            FakeTelemetryLogDao(
            initialLogs = listOf(telemetryLogEntity(id = 1L, createdAt = 1000L)),
        )
        val repository = TelemetryLogRepositoryImpl(dao)

        repository.deleteAllTelemetryLogs()

        assertEquals(emptyList(), dao.logs.first())
    }

    @Test
    fun `observeTelemetryLogsはDomainへ変換して観測する`() =
        runTest {
        val dao =
            FakeTelemetryLogDao(
            initialLogs =
                listOf(
                TelemetryLogEntity(
                    id = 1L,
                    createdAt = 2000L,
                    simulatorId = Simulator.LmuWindows.id,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root.value,
                    telemetryJson = """{"currentLap":2}""",
                ),
            ),
                )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertEquals(
            listOf(
                TelemetryLog(
                    id = 1L,
                    createdAt = 2000L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    telemetryJson = """{"currentLap":2}""",
                ),
            ),
            repository.observeTelemetryLogs().first(),
        )
    }

    @Test
    fun `observeTelemetryLogDetailは指定idのログとその一つ前のログをDomainへ変換して観測する`() =
        runTest {
        val latest = telemetryLogEntity(id = 4L, createdAt = 3000L)
        val current = telemetryLogEntity(id = 3L, createdAt = 2000L)
        val previous = telemetryLogEntity(id = 2L, createdAt = 1000L)
        val dao =
            FakeTelemetryLogDao(
            initialLogs = listOf(previous, current, latest),
        )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertEquals(
            TelemetryLogDetail(
                current = current.toDomainLog(),
                previous = previous.toDomainLog(),
            ),
            repository.observeTelemetryLogDetail(3L).first(),
        )
    }

    @Test
    fun `observeTelemetryLogDetailは同じcreatedAtの場合idが小さい直近ログをpreviousとして観測する`() =
        runTest {
        val latest = telemetryLogEntity(id = 4L, createdAt = 2000L)
        val current = telemetryLogEntity(id = 3L, createdAt = 2000L)
        val previous = telemetryLogEntity(id = 2L, createdAt = 2000L)
        val dao =
            FakeTelemetryLogDao(
            initialLogs = listOf(previous, current, latest),
        )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertEquals(
            TelemetryLogDetail(
                current = current.toDomainLog(),
                previous = previous.toDomainLog(),
            ),
            repository.observeTelemetryLogDetail(3L).first(),
        )
    }

    @Test
    fun `observeTelemetryLogDetailは指定idのログが最も古い場合previousにnullを返す`() =
        runTest {
        val current = telemetryLogEntity(id = 1L, createdAt = 1000L)
        val dao =
            FakeTelemetryLogDao(
            initialLogs =
                listOf(
                current,
                telemetryLogEntity(id = 2L, createdAt = 2000L),
            ),
                )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertEquals(
            TelemetryLogDetail(
                current = current.toDomainLog(),
                previous = null,
            ),
            repository.observeTelemetryLogDetail(1L).first(),
        )
    }

    @Test
    fun `observeTelemetryLogDetailは指定idのログがない場合nullを返す`() =
        runTest {
        val dao =
            FakeTelemetryLogDao(
            initialLogs = listOf(telemetryLogEntity(id = 1L, createdAt = 1000L)),
        )
        val repository = TelemetryLogRepositoryImpl(dao)

        assertNull(repository.observeTelemetryLogDetail(999L).first())
    }
}

private class FakeTelemetryLogDao(
    initialLogs: List<TelemetryLogEntity> = emptyList(),
) : TelemetryLogDao {
    val logs = MutableStateFlow(initialLogs)

    override fun observeTelemetryLogs(): Flow<List<TelemetryLogEntity>> = logs

    override fun observeTelemetryLog(id: Long): Flow<TelemetryLogEntity?> =
        logs.map { logs -> logs.firstOrNull { it.id == id } }

    override fun observePreviousTelemetryLog(createdAt: Long, id: Long): Flow<TelemetryLogEntity?> =
        logs.map { logs ->
            logs
                .filter { it.createdAt < createdAt || (it.createdAt == createdAt && it.id < id) }
                .maxWithOrNull(compareBy<TelemetryLogEntity> { it.createdAt }.thenBy { it.id })
        }

    override suspend fun insert(log: TelemetryLogEntity) {
        logs.update { it + log }
    }

    override suspend fun deleteAll() {
        logs.update { emptyList() }
    }
}

private fun telemetryLogEntity(
    id: Long,
    createdAt: Long,
) = TelemetryLogEntity(
    id = id,
    createdAt = createdAt,
    simulatorId = Simulator.LmuWindows.id,
    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root.value,
    telemetryJson = """{"id":$id}""",
)

private fun TelemetryLogEntity.toDomainLog() =
    TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = Simulator.fromId(simulatorId) ?: error("Unknown simulatorId: $simulatorId"),
    readoutItemKey = ReadoutItemKey.fromValue(readoutItemKey) ?: error("Unknown readoutItemKey: $readoutItemKey"),
    telemetryJson = telemetryJson,
)

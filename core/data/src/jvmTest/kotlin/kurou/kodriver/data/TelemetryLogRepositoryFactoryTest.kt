package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.TelemetryLog
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryLogRepositoryFactoryTest {
    @Test
    fun `Roomに保存したテレメトリログを観測できる`() =
        runTest {
            val directory = Files.createTempDirectory("kodriver_telemetry_log_test").toFile()
            val repository = createTelemetryLogRepository(directory.absolutePath)

            repository.saveTelemetryLog(
                createdAt = 123L,
                simulator = Simulator.Gt7Ps5,
                readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                telemetryJson = """{"current":{}}""",
            )

            assertEquals(
                listOf(
                    TelemetryLog(
                        id = 1L,
                        createdAt = 123L,
                        simulator = Simulator.Gt7Ps5,
                        readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                        telemetryJson = """{"current":{}}""",
                    ),
                ),
                repository.observeTelemetryLogs().first(),
            )
            assertTrue(directory.resolve("telemetry_logs.db").exists())
        }
}

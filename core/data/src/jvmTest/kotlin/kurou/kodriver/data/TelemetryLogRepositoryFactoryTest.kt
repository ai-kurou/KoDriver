package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.TelemetryLog
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelemetryLogRepositoryFactoryTest {
    @Test
    fun `Roomに保存したテレメトリログを観測できる`() = runTest {
        val directory = Files.createTempDirectory("kodriver_telemetry_log_test").toFile()
        val repository = createTelemetryLogRepository(directory.absolutePath)
        val log = TelemetryLog(
            createdAt = 123L,
            simulatorId = "gt7_ps5",
            readoutItemKey = "my_best_lap",
            telemetryJson = """{"current":{}}""",
        )

        repository.saveTelemetryLog(log)

        assertEquals(
            listOf(log.copy(id = 1L)),
            repository.observeTelemetryLogs().first(),
        )
        assertTrue(directory.resolve("telemetry_logs.db").exists())
    }
}

package kurou.kodriver.domain.telemetrylog.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.telemetrylog.model.NarrationOutcome
import kurou.kodriver.domain.telemetrylog.model.NarrationSource
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog
import kurou.kodriver.domain.telemetrylog.repository.TelemetryLogRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveLatestNarratedTelemetryLogUseCaseTest {
    private val repository: TelemetryLogRepository = mockk()

    @Test
    fun `最新のログを返す`() =
        runTest {
            val latest =
                TelemetryLog(
                    id = 2L,
                    createdAt = 2000L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = TestTelemetryReadoutItemKey.Flag,
                    narratedText = "イエローフラッグ",
                    narrationOutcome = NarrationOutcome.QUEUED,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = """{"currentLap":2}""",
                )
            every { repository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(latest)
            val useCase = ObserveLatestNarratedTelemetryLogUseCase(repository)

            assertEquals(latest, useCase().first())
            verify(exactly = 1) { repository.observeLatestNarratedTelemetryLog() }
            confirmVerified(repository)
        }

    @Test
    fun `ログが存在しない場合はnullを返す`() =
        runTest {
            every { repository.observeLatestNarratedTelemetryLog() } returns MutableStateFlow(null)
            val useCase = ObserveLatestNarratedTelemetryLogUseCase(repository)

            assertNull(useCase().first())
            verify(exactly = 1) { repository.observeLatestNarratedTelemetryLog() }
            confirmVerified(repository)
        }
}

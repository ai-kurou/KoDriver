package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObserveLatestTelemetryLogUseCaseTest {
    @MockK
    private lateinit var repository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `最新のログを返す`() =
        runTest {
            val latest =
                TelemetryLog(
                    id = 2L,
                    createdAt = 2000L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    narratedText = "イエローフラッグ",
                    telemetryJson = """{"currentLap":2}""",
                )
            every { repository.observeLatestTelemetryLog() } returns MutableStateFlow(latest)
            val useCase = ObserveLatestTelemetryLogUseCase(repository)

            assertEquals(latest, useCase().first())
            verify(exactly = 1) { repository.observeLatestTelemetryLog() }
            confirmVerified(repository)
        }

    @Test
    fun `ログが存在しない場合はnullを返す`() =
        runTest {
            every { repository.observeLatestTelemetryLog() } returns MutableStateFlow(null)
            val useCase = ObserveLatestTelemetryLogUseCase(repository)

            assertNull(useCase().first())
            verify(exactly = 1) { repository.observeLatestTelemetryLog() }
            confirmVerified(repository)
        }
}
